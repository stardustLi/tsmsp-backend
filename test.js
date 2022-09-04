#!/usr/bin/env node

const
	assert = require('assert'),
	crypto = require('crypto'),
	exit = () => process.exit(0),
	eq = require('util').isDeepStrictEqual,
	httpRequest = require('http').request,
	httpsRequest = require('https').request,
	_ = Symbol('fetchOnly');

assert.eq = assert.deepStrictEqual;

const APIConfig = {
	method: 'POST',
	headers: { 'content-type': 'application/json; charset=utf-8' },
	hostname: 'localhost',
	port: 6070,
	path: '/api',
	protocol: 'http:'
};

function randomChar() {
	const r = Math.random();
	if (r < 0.03) return 45;
	if (r < 0.06) return 95;
	if (r < 0.24) return crypto.randomInt(48, 58);
	if (r < 0.42) return crypto.randomInt(65, 91);
	if (r < 0.6) return crypto.randomInt(97, 123);
	return crypto.randomInt(0x4e00, 0x9fa6);
}

function randomString(length) {
	return String.fromCharCode(...Array.from({ length }, randomChar));
}

function request(config) {
	return new Promise((fulfill, reject) => {
		const req = (config.protocol.includes('https') ? httpsRequest : httpRequest)(config, res => {
			const buffers = [];
			res.on('data', chunk => buffers.push(chunk));
			res.on('end', () => fulfill([res.headers, Buffer.concat(buffers)]));
		}).on('error', reject);
		if (config.data) req.end(config.data);
		if (config.end) req.end();
	});
}

async function POST(data) {
	const content = (await request({ ...APIConfig, data: JSON.stringify(data) }))[1];
	if (process.argv.includes('--trace')) console.log(`\x1b[37m${content.toString()}\x1b[0m`);
	return JSON.parse(content);
}

async function startModule(type, test, log = 2) {
	if (log > 1) console.log(`\x1b[33m测试 \x1b[36m${type} \x1b[33m中 \x1b[0m...`);
	await test(type);
	if (log > 0) console.log(`\x1b[36m${type} \x1b[32m测试通过！\x1b[0m\n`);
}

function assertSuccess(result, value = 1) {
	assert.eq(result.status, 0);
	if (value !== _) assert.eq(result.message, value);
	return result.message;
}

function assertError(result, value = _) {
	assert.eq(result.status, -1);
	if (value !== _) assert.eq(result.message, value);
	return result.message;
}

function assertTime(time) {
	assert.eq(typeof time, 'number');
	const diff = Math.abs(new Date(time) - new Date()) / 1e3;
	if (process.argv.includes('--trace')) console.log(`\x1b[1;36m>>>>>>>>>>>>>>>> 时间差: ${diff} 秒\x1b[0m`);
	assert(diff < 30); // 30 sec
}

const ERRORS = {
	NO_PERMISSION: '错误！没有权限进行此操作',
};

async function test() {
	try {
		const
			idCard = '00000000000000001x', hzkIdCard = '00000000000000028x',
			place = { province: '卷猫', city: '猫猫', county: '真猫' };
		let userToken = '', hzkToken = '', rootToken = '';
		/*
			单元测试账号列表：
			userName	password	realName	idCard
			cat			lszzsxn		猫猫		00000000000000001x
			wide		hzkhzk		卷宽		00000000000000028x
			root		123456		管理员		400000202101081832
		*/
		{ // 登录/注册测试 (user.common)
			// 注册以及用户已存在
			await startModule('UserRegisterMessage', async type => {
				const data = {
					type,
					userName: 'cat@',
					password: 'lsz',
					realName: '猫猫',
					idCard: '2021010818'
				};

				/********/ console.log('\x1b[35m普通用户 ...\x1b[0m'); /********/

				assertError(await POST(data), '用户名 cat@ 不合法！');

				data.userName = 'cat';
				assertError(await POST(data), '密码太弱或包含非法字符！');

				data.password = 'lszzsxn';
				assertError(await POST(data), '身份证号 2021010818 不合法！');

				data.idCard = idCard.toUpperCase();
				let result1 = await POST(data);
				if (result1.status === 0) result1 = await POST(data);
				assertError(result1, '错误！用户名已经存在了');

				data.userName = 'wide', data.password = 'hzkhzk', data.realName = '卷宽';
				assertError(await POST(data), _);

				data.idCard = hzkIdCard.toUpperCase();
				await POST(data);

				/********/ console.log('\x1b[35mroot 用户 ...\x1b[0m'); /********/

				data.userName = 'root', data.password = '123456', data.realName = '管理员', data.idCard = idCard;
				assertError(await POST(data), _);

				data.idCard = '400000202101081832';
				await POST(data);
			});

			// 登录
			await startModule('UserLoginMessage', async type => {
				/********/ console.log('\x1b[35m普通用户 ...\x1b[0m'); /********/

				userToken = assertSuccess(await POST({ type, userName: 'cat', password: 'lszzsxn' }), _);
				assert.eq(typeof userToken, 'string');

				hzkToken = assertSuccess(await POST({ type, userName: 'wide', password: 'hzkhzk' }), _);
				assert.eq(typeof hzkToken, 'string');

				/********/ console.log('\x1b[35mroot 用户 ...\x1b[0m'); /********/

				rootToken = assertSuccess(await POST({ type, userName: 'root', password: '123456' }), _);
				assert.eq(typeof rootToken, 'string');
			});

			// 用户信息查询
			await startModule('UserGetProfileMessage', async type => {
				const data = { type, userToken };
				assertSuccess(
					await POST(data),
					{ userName: 'cat', password: 'lszzsxn', realName: '猫猫', idCard }
				);

				data.userToken = rootToken;
				assertSuccess(
					await POST(data),
					{ userName: 'root', password: '123456', realName: '管理员', idCard: '400000202101081832' }
				);

				data.userToken = '不存在的';
				assertError(await POST(data), '错误！用户不存在或登录信息已过期！');
			});

			// 修改密码
			await startModule('UserChangePasswordMessage', async type => {
				const tempToken = assertSuccess(
					await POST({ type, userToken, newPassword: 'AAAAAAAA' }), _
				);
				assert.eq(typeof tempToken, 'string');

				userToken = assertSuccess(
					await POST({ type, userToken: tempToken, newPassword: 'lszzsxn' }), _
				);
				assert.eq(typeof userToken, 'string');
			})
		}

		{ // 轨迹测试 (trace.common)
			// 增加轨迹
			await startModule('UserAddTraceMessage', async type => {
				const data = { type, userToken: '无效 token', idCard, trace: place };
				assertError(await POST(data), '错误！用户不存在或登录信息已过期！');

				data.userToken = userToken;
				data.idCard = 'hahaha';
				assertError(await POST(data), '错误！无权限访问 (或不存在) 身份证号为 hahaha 的用户！');

				data.idCard = hzkIdCard;
				assertError(await POST(data), `错误！无权限访问 (或不存在) 身份证号为 ${hzkIdCard} 的用户！`);

				data.idCard = idCard;
				assertSuccess(await POST(data));

				data.trace = { province: '省 2', city: '市 2', county: '区 2' };
				assertSuccess(await POST(data));

				data.trace = { province: '省 3', city: '市 3', county: '区 3' };
				assertSuccess(await POST(data));
			});

			// 获取轨迹
			let traces = [];
			await startModule('UserGetTraceMessage', async type => {
				const data = { type, userToken, idCard, startTime: 0, endTime: 1e18 };
				traces = assertSuccess(await POST(data), _);
				assert(Array.isArray(traces) && traces.length === 3);
			});

			// 更新轨迹
			await startModule('UserUpdateTraceMessage', async type => {
				const data = {
					type, userToken, idCard, time: 233,
					trace: { province: '省 4', city: '市 4', county: '区 4' }
				};
				assertSuccess(await POST(data), 0);

				data.time = traces.at(-1).time;
				data.trace = { province: '省 5', city: '市 5', county: '区 5' };
				assertSuccess(await POST(data));
			});

			// 删除轨迹
			await startModule('UserDeleteTraceMessage', async type => {
				const data = { type, userToken, idCard, time: 233 };
				assertSuccess(await POST(data), 0);

				data.time = traces.at(-2).time;
				assertSuccess(await POST(data));
				traces.splice(-2, 1);
			});

			console.log('\x1b[35m检查是否成功删除 ...\x1b[0m\n');
			await startModule('UserGetTraceMessage', async type => {
				const data = { type, userToken, idCard, startTime: 0, endTime: 1e18 };
				const result = assertSuccess(await POST(data), _);
				assert(Array.isArray(result) && result.length === 2);
			}, 0);

			console.log('\x1b[35m删除剩余全部数据 ...\x1b[0m\n');
			await startModule('UserDeleteTraceMessage', async type => {
				await Promise.all(
					traces.map(async trace =>
						assertSuccess(await POST({ type, userToken, idCard, time: trace.time }))
					)
				);
			}, 0);
		}

		{ // 贴贴轨迹测试 (trace.withPeople)
			// 上报密接
			await startModule('UserAddTraceWithPeopleMessage', async type => {
				const data = { type, userToken, idCard, cc: 'no-such-user' };
				assertError(await POST(data), _);

				data.cc = 'wide';
				assertSuccess(await POST(data));

				data.cc = 'root';
				assertSuccess(await POST(data));

				data.cc = 'wide';
				assertSuccess(await POST(data));
			});

			// 获取轨迹
			let traces = [];
			await startModule('UserGetTraceWithPeopleMessage', async type => {
				const data = { type, userToken, idCard, startTime: 0, endTime: 1e18 };
				traces = assertSuccess(await POST(data), _);
				assert(Array.isArray(traces) && traces.length === 3);
			});

			// 更新轨迹
			await startModule('UserUpdateTraceWithPeopleMessage', async type => {
				const data = { type, userToken, idCard, time: 233, cc: 'cat' };
				assertSuccess(await POST(data), 0);

				data.time = traces.at(-1).time;
				assertSuccess(await POST(data));
			});

			// 删除轨迹
			await startModule('UserDeleteTraceWithPeopleMessage', async type => {
				const data = { type, userToken, idCard, time: 233 };
				assertSuccess(await POST(data), 0);

				data.time = traces.at(-2).time;
				assertSuccess(await POST(data));
				traces.splice(-2, 1);
			});

			// wide & cat
			console.log('\x1b[35m检查是否成功删除 ...\x1b[0m\n');
			await startModule('UserGetTraceWithPeopleMessage', async type => {
				const data = { type, userToken, idCard, startTime: 0, endTime: 1e18 };
				const result = assertSuccess(await POST(data), _);
				assert(
					Array.isArray(result) &&
					result.length === 2 &&
					result[0].CCUserName === 'wide' &&
					result[1].CCUserName === 'cat' &&
					!Object.hasOwn(result[0], 'CCIDCard')
				);
			}, 0);

			await startModule('UserGetTraceWithPeopleWithIDCardMessage', async type => {
				const data = { type, userToken, idCard, startTime: 0, endTime: 1e18 };
				assertError(await POST(data), ERRORS.NO_PERMISSION);

				data.userToken = rootToken;
				const result = assertSuccess(await POST(data), _);
				assert(
					Array.isArray(result) &&
					result.length === 2 &&
					result[0].CCUserName === 'wide' &&
					result[1].CCUserName === 'cat' &&
					result[0].CCIDCard === hzkIdCard &&
					result[1].CCIDCard === idCard
				);
			});

			console.log('\x1b[35m删除剩余全部数据 ...\x1b[0m\n');
			await startModule('UserDeleteTraceWithPeopleMessage', async type => {
				await Promise.all(
					traces.map(async trace =>
						assertSuccess(await POST({ type, userToken, idCard, time: trace.time }))
					)
				);
			}, 0);
		}

		{ // 政策测试 (policy)
			// 政策修改和查询
			const policies = ['卷宽卷宽喵希喵希！', '猫猫和真猫贴贴！'];

			for (let t = 0; t < 2; ++t) {
				console.log(`\x1b[35m政策修改第 \x1b[32m${t + 1}/2\x1b[35m 轮 ...\x1b[0m`);
				await startModule('PolicyUpdateMessage', async type => {
					const data = { type, userToken, place, content: policies[t] };
					assertError(await POST(data), ERRORS.NO_PERMISSION);

					data.userToken = rootToken;
					assertSuccess(await POST(data));
				});

				await startModule('PolicyQueryMessage', async type => {
					const data = { type, place };
					assertSuccess(await POST(data), policies[t]);
				});
			}

			// 不存在以及不合法政策测试
			console.log('\x1b[35m不存在以及不合法政策测试 ...\x1b[0m\n');
			await startModule('PolicyQueryMessage', async type => {
				const data = { type, place: { province: '不存在的', city: '坏了', county: '没了' } };
				assertSuccess(await POST(data), null);

				data.place = { '猫猫': '宽宽' };
				try {
					await POST(data);
					throw new Error();
				} catch (e) {
					assert.eq(e.message, 'Unexpected token U in JSON at position 0');
				}
			}, 0);

			// 政策继承测试
			console.log('\x1b[1;35m政策继承测试 ...\x1b[0m\n');
			await startModule('PolicyUpdateMessage', async type => {
				const data = { type, userToken: rootToken, place: { province: '卷猫', city: '', county: '' }, content: '猫宽学！' };
				assertSuccess(await POST(data));

				const qdata = { type: 'PolicyQueryMessage', place };
				assertSuccess(await POST(qdata), policies[1]);

				qdata.place = { ...place, county: '假猫' };
				assertSuccess(await POST(qdata), '猫宽学！');

				data.content = '';
				assertSuccess(await POST(data));

				qdata.place.county = '真猫';
				assertSuccess(await POST(qdata), policies[1]);

				qdata.place.county = '假猫';
				assertSuccess(await POST(qdata), null);

				data.content = '欢迎来到猫宽世界！';
				assertSuccess(await POST(data));

				qdata.place.county = '真猫';
				assertSuccess(await POST(qdata), policies[1]);

				qdata.place.county = '假猫';
				assertSuccess(await POST(qdata), '欢迎来到猫宽世界！');
			}, 0);
		}

		{ // 风险区测试 (code.`DangerousPlaceMessage` / `SetDangerousPlaceMessage`)
			// 风险区修改和查询
			for (let t = 0; t < 3; ++t) {
				console.log(`\x1b[35m风险区修改第 \x1b[32m${t + 1}/3\x1b[35m 轮 ...\x1b[0m`);
				await startModule('SetDangerousPlaceMessage', async type => {
					const data = { type, userToken, place, level: t };
					assertError(await POST(data), ERRORS.NO_PERMISSION);

					data.userToken = rootToken;
					assertSuccess(await POST(data));
				});

				await startModule('DangerousPlaceMessage', async type => {
					const data = { type, place };
					assertSuccess(await POST(data), t);
				});
			}

			console.log('\x1b[35m不存在以及不合法地区测试 ...\x1b[0m\n');
			await startModule('DangerousPlaceMessage', async type => {
				const data = { type, place: { province: '不存在的', city: '坏了', county: '没了' } };
				assertError(await POST(data), _);

				data.place = { '猫猫': '宽宽' };
				try {
					await POST(data);
					throw new Error();
				} catch (e) {
					assert.eq(e.message, 'Unexpected token U in JSON at position 0');
				}
			}, 0);
		}

		{ // 管理权限更改测试 (user.admin)
			const permission = { userName: 'cat', setRiskAreas: true };
			await startModule('GetAdminPermissionMessage / SetAdminPermissionMessage', async () => {
				const
					dataGet = { type: 'GetAdminPermissionMessage', userToken },
					dataSet = { type: 'SetAdminPermissionMessage', userToken, permission };
				assertError(await POST(dataSet), ERRORS.NO_PERMISSION);

				dataSet.userToken = rootToken;
				assertSuccess(await POST(dataSet));

				const result1 = assertSuccess(await POST(dataGet), _);
				assert(Object.entries(result1).every(([key, value]) => 'setRiskAreas' === key === value || key === 'userName'));

				{
					const data1 = { type: 'SetDangerousPlaceMessage', userToken, place, level: 2 };
					assertSuccess(await POST(data1));

					const data2 = { type: 'DangerousPlaceMessage', place };
					assertSuccess(await POST(data2), 2);

					const data3 = { type: 'PolicyUpdateMessage', userToken, place, content: '改不了的' };
					assertError(await POST(data3), ERRORS.NO_PERMISSION);
				}

				dataSet.permission = { userName: 'cat', setPolicy: true };
				assertSuccess(await POST(dataSet));

				const result2 = assertSuccess(await POST(dataGet), _);
				assert(Object.entries(result2).every(([key, value]) => 'setPolicy' === key === value || key === 'userName'));

				{
					const data1 = { type: 'PolicyUpdateMessage', userToken, place, content: '卷宽太坏了' };
					assertSuccess(await POST(data1));

					const data2 = { type: 'PolicyQueryMessage', place };
					assertSuccess(await POST(data2), '卷宽太坏了');

					const data3 = { type: 'SetDangerousPlaceMessage', userToken, place, level: 1 };
					assertError(await POST(data3), ERRORS.NO_PERMISSION);
				}

				dataSet.permission = { userName: 'cat' };
				assertSuccess(await POST(dataSet));

				const result3 = assertSuccess(await POST(dataGet), _);
				assert(Object.entries(result3).every(([key, value]) => value === false || key === 'userName'));
			});
		}

		{ // 申诉测试 (code.appeal)
			// 添加状态申诉
			await startModule('UserAppealMessage', async type => {
				const data = { type, userToken, idCard, reason: '我要抱猫猫！' };
				let result = await POST(data);
				if (result.status === 0) {
					assert.eq(result.message, 1);
					result = await POST(data);
				}
				assertError(result, _);

				data.reason = '我还要抱宽宽！';
				assertError(await POST(data), _);
			});

			// 查询申诉
			await startModule('QueryAppealMessage', async type => {
				const data = { type, userToken, idCard };
				assertError(await POST(data), ERRORS.NO_PERMISSION);

				assertSuccess(await POST({
					type: 'SetAdminPermissionMessage',
					userToken: rootToken,
					permission: { userName: 'cat', viewAppeals: true }
				}));

				const result1 = assertSuccess(await POST(data), _);
				assert.eq(result1.idCard, idCard);
				assert.eq(result1.reason, '我要抱猫猫！');
				assertTime(result1.time);

				data.idCard = hzkIdCard;
				assertSuccess(await POST(data), null);

				assertSuccess(await POST({
					type: 'SetAdminPermissionMessage',
					userToken: rootToken,
					permission: { userName: 'cat' }
				}));

				data.idCard = idCard;
				assertError(await POST(data), ERRORS.NO_PERMISSION);
			});

			// 解决申诉
			await startModule('ResolveAppealMessage', async type => {
				const data = { type, userToken: rootToken, idCard };
				assertSuccess(await POST(data));

				assertSuccess(await POST(data), 0);

				assertSuccess(await POST({ type: 'QueryAppealMessage', userToken: rootToken, idCard }), null);
			});
		}

		{ // 进京报备 (code.`JingReportMessage`)
			// 添加进京报备
			await startModule('JingReportMessage', async type => {
				const data = { type, userToken, idCard, reason: '猫猫抱起来舒服！' };
				let result = await POST(data);
				if (result.status === 0) {
					assert.eq(result.message, 1);
					result = await POST(data);
				}
				assertError(result, _);

				data.reason = '宽宽抱起来不舒服！';
				assertError(await POST(data), _);
			});
		}

		{ // 权限授予测试 (user.permission)
			// 权限授予
			await startModule('UserGrantPermissionMessage', async type => {
				const data = { type: 'UserAppealMessage', userToken, idCard: hzkIdCard, reason: '没有权限的猫猫~' };
				assertError(await POST(data), `错误！无权限访问 (或不存在) 身份证号为 ${hzkIdCard} 的用户！`);

				assertSuccess(await POST({ type, userToken: hzkToken, other: 'cat' }));

				data.reason = '有权限的猫猫！';
				assertSuccess(await POST(data), 1);

				const result = assertSuccess(
					await POST({ type: 'QueryAppealMessage', userToken: rootToken, idCard: hzkIdCard }), _
				);

				assert.eq(result.idCard, hzkIdCard);
				assert.eq(result.reason, '有权限的猫猫！');
				assertTime(result.time);

				assertSuccess(await POST({ type: 'ResolveAppealMessage', userToken: rootToken, idCard: hzkIdCard }));
			});

			// 权限查看
			await startModule('UserFetchAllGrantedUsersMessage', async type => {
				const data = { type, userToken: hzkToken };
				assertSuccess(await POST(data), ['cat']);
			});

			// 权限撤销
			await startModule('UserRevokePermissionMessage', async type => {
				const data = { type: 'UserAppealMessage', userToken, idCard: hzkIdCard, reason: '没有权限的猫猫~' };

				assertSuccess(await POST({ type, userToken: hzkToken, other: 'cat' }));

				assertError(await POST(data), `错误！无权限访问 (或不存在) 身份证号为 ${hzkIdCard} 的用户！`);

				assertSuccess(await POST({ type: 'UserFetchAllGrantedUsersMessage', userToken: hzkToken }), []);
			});
		}

		{ // 疫苗测试 (vaccine)
			const
				Tbegin = new Date('2020-1-1 00:00:00'), Tend = new Date(),
				manufacture = '猫宽疫苗机构',
				time1 = crypto.randomInt(Tbegin.getTime(), Tend.getTime()),
				time2 = crypto.randomInt(Tbegin.getTime(), Tend.getTime());
			let vaccines = [];
			// 获取疫苗记录
			await startModule('UserGetVaccineMessage', async type => {
				const data = { type, userToken, idCard };
				vaccines = assertSuccess(await POST(data), _);
				assert(Array.isArray(vaccines));
			});

			vaccines.push(
				{ idCard, manufacture: manufacture + ' A', time: time1, vaccineType: vaccines.length + 1 },
				{ idCard, manufacture: manufacture + ' B', time: time2, vaccineType: vaccines.length + 2 },
			);
			// 添加疫苗
			await startModule('UserAddVaccineMessage', async type => {
				const data = { type, userToken, idCard };
				assertSuccess(await POST({ ...data, manufacture: manufacture + ' A', time: time1 }));
				assertSuccess(await POST({ ...data, manufacture: manufacture + ' B', time: time2 }));
				assertSuccess(await POST({ type: 'UserGetVaccineMessage', userToken, idCard }), vaccines);
			});
		}

		{ // 核酸测试 (nucleicAcidTest)
			const name = randomString(20);
			console.log(`\x1b[35m增加核酸测试点：\x1b[32m${name}\x1b[0m\n`);

			const position = { approximatePlace: place, street: '猫街' };

			// 增加核酸测试点
			await startModule('AddNucleicAcidTestPointMessage', async type => {
				const data = { type, userToken: rootToken, place: position, name };
				assertError(
					await POST({ ...data, name: '@<-这是非法字符' }),
					'核酸测试点名称 @<-这是非法字符 不合法！'
				);
				assertSuccess(await POST(data));
				assertError(await POST(data), _);
			});

			// 获取核酸测试点
			await startModule('GetAllNucleicAcidTestPointMessage', async type => {
				const data = { type };
				const result = assertSuccess(await POST(data), _);
				assert(result.some(w => eq(w, { place: position, name })));
			});

			// 预约核酸
			await startModule('AppointNucleicAcidTestMessage', async type => {
				const data = { type, userToken, idCard, testPlace: name };
				assertError(
					await POST({ ...data, testPlace: '这个地点应当不存在' }),
					'核酸测试点 这个地点应当不存在 不存在！'
				);
				assertSuccess(await POST(data));
				assertError(await POST(data), `错误！身份证号为 ${idCard} 的核酸预约已存在`)
			});

			// 查询核酸预约点排队人数
			await startModule('QueryTestPointWaitingPersonMessage', async type => {
				const data = { type, place: name };
				assertSuccess(await POST(data), 1);
			});

			// 查询核酸预约点所有人
			await startModule('AdminQueryTestPointWaitingPersonMessage', async type => {
				const data = { type, userToken, place: name };
				assertError(await POST(data), ERRORS.NO_PERMISSION);

				data.userToken = rootToken;
				const result = assertSuccess(await POST(data), _);
				assert.eq(result.length, 1);
				assert.eq(result[0].idCard, idCard);
				assert.eq(result[0].testPlace, name);
				assertTime(result[0].appointTime);
			});

			// 完成核酸
			await startModule('FinishNucleicAcidTestMessage', async type => {
				const data = { type, userToken, idCard, testPlace: name, nucleicResult: true };
				assertError(await POST(data), ERRORS.NO_PERMISSION);

				data.userToken = rootToken;
				assertSuccess(await POST(data));
				assertError(await POST(data), `错误！身份证号为 ${idCard} 的用户未进行预约`)
			});

			// 获取核酸测试结果
			await startModule('GetNucleicAcidTestResultsMessage', async type => {
				const data = { type, userToken, idCard };
				const result = assertSuccess(await POST(data), _).filter(r => r.testPlace === name);
				assert.eq(result.length, 1);
				assert.eq(result[0].idCard, idCard);
				assert.eq(result[0].result, true);
				assertTime(result[0].time);
			});
		}

		{ // 健康码颜色测试 (code.`UserGetColorMessage` / `AdminSetColorMessage`)
			await startModule('UserGetColorMessage', async type => {
				const
					place2 = {province: 'A 省', city: 'B 市', county: 'C 区'},
					makeHzk = { userToken: hzkToken, idCard: hzkIdCard },
					dataGet = { type, userToken, idCard },
					dataGetHzk = { ...dataGet, ...makeHzk },
					dataSet = { type: 'AdminSetColorMessage', userToken: rootToken, idCard },
					dataSetHzk = { ...dataSet, idCard: hzkIdCard },
					traceAdd = { type: 'UserAddTraceMessage', userToken, idCard },
					traceAddHzk = { ...traceAdd, ...makeHzk },
					traceGet = { type: 'UserGetTraceMessage', userToken, idCard, startTime: 0, endTime: 1e18 },
					traceGetHzk = { ...traceGet, ...makeHzk },
					traceDelete = { type: 'UserDeleteTraceMessage', userToken, idCard },
					traceDeleteHzk = { ...traceDelete, ...makeHzk },
					ccAdd = { type: 'UserAddTraceWithPeopleMessage', userToken, idCard },
					ccAddHzk = { ...ccAdd, ...makeHzk },
					ccGet = { type: 'UserGetTraceWithPeopleMessage', userToken, idCard, startTime: 0, endTime: 1e18 },
					ccGetHzk = { ...ccGet, ...makeHzk },
					ccDelete = { type: 'UserDeleteTraceWithPeopleMessage', userToken, idCard },
					ccDeleteHzk = { ...ccDelete, ...makeHzk },
					dataRisk = { type: 'SetDangerousPlaceMessage', userToken: rootToken, place };
				// 默认绿码
				assertSuccess(await POST(dataGet), 0);
				// 添加轨迹
				assertSuccess(await POST({ ...traceAdd, trace: place }));
				// 设置低风险区
				assertSuccess(await POST({ ...dataRisk, level: 0 }));
				// 仍为绿码
				assertSuccess(await POST(dataGet), 0);
				// 设置中风险区
				assertSuccess(await POST({ ...dataRisk, level: 1 }));
				// 为黄码
				assertSuccess(await POST(dataGet), 2);
				// 设置高风险区
				assertSuccess(await POST({ ...dataRisk, level: 2 }));
				// 为红码
				assertSuccess(await POST(dataGet), 3);
				// 设置中风险区
				assertSuccess(await POST({ ...dataRisk, level: 1 }));
				// 仍为红码
				assertSuccess(await POST(dataGet), 3);
				// 查询轨迹
				const traces1 = assertSuccess(await POST(traceGet), _)[0];
				// 移除轨迹
				assertSuccess(await POST({ ...traceDelete, time: traces1.time }));
				// 仍为红码
				assertSuccess(await POST(dataGet), 3);
				// 管理员赋绿码
				assertSuccess(await POST({ ...dataSet, color: 0 }), 1);
				// 为绿码
				assertSuccess(await POST(dataGet), 0);
				// 加回轨迹 (中风险)
				assertSuccess(await POST({ ...traceAdd, trace: place }));
				// 为黄码
				assertSuccess(await POST(dataGet), 2);

				// hzk 为绿码
				assertSuccess(await POST(dataGetHzk), 0);
				// hzk 增加密接
				assertSuccess(await POST({ ...ccAddHzk, cc: 'cat' }));
				// hzk 为弹窗
				assertSuccess(await POST(dataGetHzk), 1);
				// hzk 去中风险地区
				assertSuccess(await POST({ ...traceAddHzk, trace: place }));
				// hzk 为黄码
				assertSuccess(await POST(dataGetHzk), 2);

				// hzk 移除轨迹
				const traces2 = assertSuccess(await POST(traceGetHzk), _)[0];
				assertSuccess(await POST({ ...traceDeleteHzk, time: traces2.time }));
				const traces3 = assertSuccess(await POST(ccGetHzk), _)[0];
				assertSuccess(await POST({ ...ccDeleteHzk, time: traces3.time }));
				// 管理员赋绿码
				assertSuccess(await POST({ ...dataSetHzk, color: 0 }), 1);
				// 为绿码
				assertSuccess(await POST(dataGetHzk), 0);

				// 添加轨迹
				assertSuccess(await POST({ ...traceAdd, trace: place2 }));
				// 为黄码
				assertSuccess(await POST(dataGet), 2);
				// 设置低风险地区
				assertSuccess(await POST({ ...dataRisk, place: place2, level: 0 }));
				// 为黄码
				assertSuccess(await POST(dataGet), 2);
				// 设置高风险地区
				assertSuccess(await POST({ ...dataRisk, place: place2, level: 2 }));
				// 为红码
				assertSuccess(await POST(dataGet), 3);

				// hzk 为绿码
				assertSuccess(await POST(dataGetHzk), 0);
				// hzk 增加密接
				assertSuccess(await POST({ ...ccAddHzk, cc: 'cat' }));
				// hzk 为黄码
				assertSuccess(await POST(dataGetHzk), 2);

				// 重置低风险
				assertSuccess(await POST({ ...dataRisk, place: place2, level: 0 }));

				// 移除轨迹
				const traces4 = assertSuccess(await POST(traceGet), _);
				assert.eq(traces4.length, 2);
				assertSuccess(await POST({ ...traceDelete, time: traces4[0].time }));
				assertSuccess(await POST({ ...traceDelete, time: traces4[1].time }));
				const traces5 = assertSuccess(await POST(ccGetHzk), _);
				assertSuccess(await POST({ ...ccDeleteHzk, time: traces5[0].time }));

				// 管理员赋绿码
				assertSuccess(await POST({ ...dataSet, color: 0 }), 1);
				// 为绿码
				assertSuccess(await POST(dataGet), 0);
				// 管理员赋绿码
				assertSuccess(await POST({ ...dataSetHzk, color: 0 }), 1);
				// 为绿码
				assertSuccess(await POST(dataGetHzk), 0);
			});
		}
	} catch (e) {
		console.log('测试失败，错误:', e);
	}
}

test();

/*

await startModule('<message type>Message', async type => {
	const data = { type, userToken, ... };
	assertSuccess(await POST(data));
});

*/
