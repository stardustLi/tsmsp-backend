#!/usr/bin/env node

const
	assert = require('assert'),
	crypto = require('crypto'),
	exit = () => process.exit(0),
	{ isDeepStrictEqual: eq, isString } = require('util'),
	httpRequest = require('http').request,
	httpsRequest = require('https').request,
	_ = Symbol('fetchOnly');

const APIConfig = {
	method: 'POST',
	headers: { 'content-type': 'application/json; charset=utf-8' },
	hostname: 'localhost',
	port: 6070,
	path: '/api',
	protocol: 'http:'
};

const stringUtil = {
	randomChar() {
		const r = Math.random();
		if (r < 0.03) return 45;
		if (r < 0.06) return 95;
		if (r < 0.24) return crypto.randomInt(48, 58);
		if (r < 0.42) return crypto.randomInt(65, 91);
		if (r < 0.6) return crypto.randomInt(97, 123);
		return crypto.randomInt(0x4e00, 0x9fa6);
	},

	randomString(length) {
		return String.fromCharCode(...Array.from({ length }, stringUtil.randomChar));
	},
};

const webUtil = {
	request(config) {
		return new Promise((fulfill, reject) => {
			const req = (config.protocol.includes('https') ? httpsRequest : httpRequest)(config, res => {
				const buffers = [];
				res.on('data', chunk => buffers.push(chunk));
				res.on('end', () => fulfill([res.headers, Buffer.concat(buffers)]));
			}).on('error', reject);
			if (config.data) req.end(config.data);
			if (config.end) req.end();
		});
	},

	async POST(data) {
		const content = (await webUtil.request({ ...APIConfig, data: JSON.stringify(data) }))[1];
		if (process.argv.includes('--trace')) console.log(`\x1b[37m${content.toString()}\x1b[0m`);
		return JSON.parse(content);
	},
}

assert.eq = assert.deepStrictEqual;

assert.success = (result, value = 1) => {
	assert.eq(result.status, 0);
	if (typeof value === 'function') assert(value(result.message));
	else if (value !== _) assert.eq(result.message, value);
	return result.message;
}

assert.error = (result, value = _) => {
	assert.eq(result.status, -1);
	if (typeof value === 'function') assert(value(result.message));
	else if (value !== _) assert.eq(result.message, value);
	return result.message;
}

assert.time = time => {
	assert.eq(typeof time, 'number');
	const diff = Math.abs(new Date(time) - new Date()) / 1e3;
	if (process.argv.includes('--trace')) console.log(`\x1b[1;36m>>>>>>>>>>>>>>>> 时间差: ${diff} 秒\x1b[0m`);
	assert(diff < 30); // 30 sec
}

const ERRORS = {
	NO_PERMISSION: '错误！没有权限进行此操作',
};

async function test() {
	async function startModule(type, test, log = 2) {
		if (log > 1) console.log(`\x1b[33m测试 \x1b[36m${type} \x1b[33m中 \x1b[0m...`);
		await test(type);
		if (log > 0) console.log(`\x1b[36m${type} \x1b[32m测试通过！\x1b[0m\n`);
	}

	try {
		const
			{ POST } = webUtil,
			{ success, error, time } = assert,
			idCard = '00000000000000001x', hzkIdCard = '00000000000000028x',
			placeStructure = [];
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

				error(await POST(data), '用户名 cat@ 不合法！');

				data.userName = 'cat';
				error(await POST(data), '密码太弱或包含非法字符！');

				data.password = 'lszzsxn';
				error(await POST(data), '身份证号 2021010818 不合法！');

				data.idCard = idCard.toUpperCase();
				let result1 = await POST(data);
				if (result1.status === 0) result1 = await POST(data);
				error(result1, '错误！用户名已经存在了');

				data.userName = 'wide', data.password = 'hzkhzk', data.realName = '卷宽';
				error(await POST(data), _);

				data.idCard = hzkIdCard.toUpperCase();
				await POST(data);

				/********/ console.log('\x1b[35mroot 用户 ...\x1b[0m'); /********/

				data.userName = 'root', data.password = '123456', data.realName = '管理员', data.idCard = idCard;
				error(await POST(data), _);

				data.idCard = '400000202101081832';
				await POST(data);
			});

			// 登录
			await startModule('UserLoginMessage', async type => {
				/********/ console.log('\x1b[35m普通用户 ...\x1b[0m'); /********/
				userToken = success(await POST({ type, userName: 'cat', password: 'lszzsxn' }), isString);
				hzkToken = success(await POST({ type, userName: 'wide', password: 'hzkhzk' }), isString);

				/********/ console.log('\x1b[35mroot 用户 ...\x1b[0m'); /********/
				rootToken = success(await POST({ type, userName: 'root', password: '123456' }), isString);
			});

			// 用户信息查询
			await startModule('UserGetProfileMessage', async type => {
				const data = { type, userToken };
				success(
					await POST(data),
					{ userName: 'cat', password: 'lszzsxn', realName: '猫猫', idCard }
				);

				data.userToken = rootToken;
				success(
					await POST(data),
					{ userName: 'root', password: '123456', realName: '管理员', idCard: '400000202101081832' }
				);

				data.userToken = '不存在的';
				error(await POST(data), '错误！用户不存在或登录信息已过期！');
			});

			// 修改密码
			await startModule('UserChangePasswordMessage', async type => {
				const tempToken = success(
					await POST({ type, userToken, newPassword: 'AAAAAAAA' }), _
				);
				assert.eq(typeof tempToken, 'string');

				userToken = success(
					await POST({ type, userToken: tempToken, newPassword: 'lszzsxn' }), _
				);
				assert.eq(typeof userToken, 'string');
			})
		}

		{ // 地点测试 (trace)
			placeStructure.push(null);
			placeStructure.push({ id: 1, name: '卷猫', level: 0, parent: placeStructure[0] });
			placeStructure.push({ id: 2, name: '猫猫', level: 1, parent: placeStructure[1] });
			placeStructure.push({ id: 3, name: '真猫', level: 2, parent: placeStructure[2] });
			placeStructure.push({ id: 4, name: '猫街', level: 3, parent: placeStructure[3] });
			placeStructure.push({ id: 5, name: '假猫', level: 2, parent: placeStructure[2] });
			placeStructure.push({ id: 6, name: '水街', level: 3, parent: placeStructure[5] });
			placeStructure.push({ id: 7, name: '新猫', level: 2, parent: placeStructure[2] });
			placeStructure.push({ id: 8, name: '猫街', level: 3, parent: placeStructure[7] });
			placeStructure.push({ id: 9, name: '猫希', level: 1, parent: placeStructure[1] });
			placeStructure.push({ id: 10, name: '猫涵', level: 2, parent: placeStructure[9] });
			placeStructure.push({ id: 11, name: '猫涵', level: 1, parent: placeStructure[1] });
			placeStructure.push({ id: 12, name: '省 2', level: 0, parent: placeStructure[0] });
			placeStructure.push({ id: 13, name: '市 2', level: 1, parent: placeStructure[12] });
			placeStructure.push({ id: 14, name: '区 2', level: 2, parent: placeStructure[13] });
			placeStructure.push({ id: 15, name: '街 2', level: 3, parent: placeStructure[14] });

			// 添加地点
			await startModule('CreatePlaceMessage', async type => {
				const data = { type, userToken, traceDescriptor: { '猫猫': '宽宽' } };
				try {
					await POST(data);
					throw new Error;
				} catch (e) {
					assert.eq(e.message, 'Unexpected token C in JSON at position 0');
				}

				data.traceDescriptor = [];
				error(await POST(data), ERRORS.NO_PERMISSION);

				data.userToken = rootToken;
				error(await POST(data), '地点层级结构是空的！');

				data.traceDescriptor = ['卷猫', '猫猫', '真猫', '猫街', '小猫'];
				error(await POST(data), '地点层级结构太长了！');

				data.traceDescriptor.pop();
				success(await POST(data), placeStructure[4]);

				data.traceDescriptor = ['卷猫', '猫猫', '假猫', '水街'];
				success(await POST(data), placeStructure[6]);

				data.traceDescriptor = ['卷猫', '猫猫', '新猫', '猫街'];
				success(await POST(data), placeStructure[8]);

				data.traceDescriptor = ['卷猫', '猫希'];
				success(await POST(data), placeStructure[9]);

				data.traceDescriptor = ['卷猫', '猫希', '猫涵'];
				success(await POST(data), placeStructure[10]);

				data.traceDescriptor = ['卷猫', '猫涵'];
				success(await POST(data), placeStructure[11]);

				data.traceDescriptor = ['省 2', '市 2', '区 2', '街 2'];
				success(await POST(data), placeStructure[15]);
			});

			// 查询地点
			await startModule('GetPlaceInfoMessage', async type => {
				await Promise.all(
					placeStructure.map(async (place, idx) => {
						if (!idx) return;
						success(await POST({ type, traceID: idx }), place);
					})
				);
			});

			placeStructure.map((place, idx) => {
				if (!idx) return;
				place.subtree = [];
			});
			placeStructure[0] = { subtree: [] };
			placeStructure.map((place, idx) => {
				if (!idx) return;
				(place.parent ?? placeStructure[0]).subtree.push(
					{ id: place.id, name: place.name, level: place.level }
				);
			})

			// 查询下属地点
			await startModule('GetPlaceSubordinatesMessage', async type => {
				await Promise.all(
					placeStructure.map(async (place, idx) =>
						success(await POST({ type, traceID: idx }), place.subtree)
					)
				);
			});
		}

		{ // 轨迹测试 (trace.common)
			// 增加轨迹
			await startModule('UserAddTraceMessage', async type => {
				const data = { type, userToken: '无效 token', idCard, trace: 1 };
				error(await POST(data), '错误！用户不存在或登录信息已过期！');

				data.userToken = userToken;
				data.idCard = 'hahaha';
				error(await POST(data), '错误！无权限访问 (或不存在) 身份证号为 hahaha 的用户！');

				data.idCard = hzkIdCard;
				error(await POST(data), `错误！无权限访问 (或不存在) 身份证号为 ${hzkIdCard} 的用户！`);

				data.idCard = idCard;
				success(await POST(data));

				data.trace = 2;
				success(await POST(data));

				data.trace = 3;
				success(await POST(data));
			});

			// 获取轨迹
			let traces = [];
			await startModule('UserGetTraceMessage', async type => {
				const data = { type, userToken, idCard, startTime: 0, endTime: 1e18 };
				traces = success(await POST(data), _);
				assert(Array.isArray(traces) && traces.length === 3);
			});

			// 更新轨迹
			await startModule('UserUpdateTraceMessage', async type => {
				const data = { type, userToken, idCard, time: 233, trace: 4 };
				success(await POST(data), 0);

				data.time = traces.at(-1).time;
				data.trace = 5;
				success(await POST(data));
			});

			// 删除轨迹
			await startModule('UserDeleteTraceMessage', async type => {
				const data = { type, userToken, idCard, time: 233 };
				success(await POST(data), 0);

				data.time = traces.at(-2).time;
				success(await POST(data));
				traces.splice(-2, 1);
			});

			console.log('\x1b[35m检查是否成功删除 ...\x1b[0m\n');
			await startModule('UserGetTraceMessage', async type => {
				const data = { type, userToken, idCard, startTime: 0, endTime: 1e18 };
				const result = success(await POST(data), _);
				assert(Array.isArray(result) && result.length === 2);
			}, 0);

			console.log('\x1b[35m删除剩余全部数据 ...\x1b[0m\n');
			await startModule('UserDeleteTraceMessage', async type => {
				await Promise.all(
					traces.map(async trace =>
						success(await POST({ type, userToken, idCard, time: trace.time }))
					)
				);
			}, 0);
		}

		{ // 贴贴轨迹测试 (trace.withPeople)
			// 上报密接
			await startModule('UserAddTraceWithPeopleMessage', async type => {
				const data = { type, userToken, idCard, cc: 'no-such-user' };
				error(await POST(data), _);

				data.cc = 'wide';
				success(await POST(data));

				data.cc = 'root';
				success(await POST(data));

				data.cc = 'wide';
				success(await POST(data));
			});

			// 获取轨迹
			let traces = [];
			await startModule('UserGetTraceWithPeopleMessage', async type => {
				const data = { type, userToken, idCard, startTime: 0, endTime: 1e18 };
				traces = success(await POST(data), _);
				assert(Array.isArray(traces) && traces.length === 3);
			});

			// 更新轨迹
			await startModule('UserUpdateTraceWithPeopleMessage', async type => {
				const data = { type, userToken, idCard, time: 233, cc: 'cat' };
				success(await POST(data), 0);

				data.time = traces.at(-1).time;
				success(await POST(data));
			});

			// 删除轨迹
			await startModule('UserDeleteTraceWithPeopleMessage', async type => {
				const data = { type, userToken, idCard, time: 233 };
				success(await POST(data), 0);

				data.time = traces.at(-2).time;
				success(await POST(data));
				traces.splice(-2, 1);
			});

			// wide & cat
			console.log('\x1b[35m检查是否成功删除 ...\x1b[0m\n');
			await startModule('UserGetTraceWithPeopleMessage', async type => {
				const data = { type, userToken, idCard, startTime: 0, endTime: 1e18 };
				const result = success(await POST(data), _);
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
				error(await POST(data), ERRORS.NO_PERMISSION);

				data.userToken = rootToken;
				const result = success(await POST(data), _);
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
						success(await POST({ type, userToken, idCard, time: trace.time }))
					)
				);
			}, 0);
		}

		{ // 政策测试 (policy)
			// 政策修改和查询
			const policies = [
				'卷宽卷宽喵希喵希！', '猫猫和真猫贴贴！',
				'市级政策', '省级政策'
			];

			for (let t = 0; t < 2; ++t) {
				console.log(`\x1b[35m政策修改第 \x1b[32m${t + 1}/2\x1b[35m 轮 ...\x1b[0m`);
				await startModule('PolicyUpdateMessage', async type => {
					const data = { type, userToken, place: 3, content: policies[t] };
					error(await POST(data), ERRORS.NO_PERMISSION);

					data.userToken = rootToken;
					success(await POST(data));
				});

				await startModule('PolicyQueryMessage', async type => {
					success(await POST({ type, place: 3 }), policies[t]);
				});
			}

			// 不存在、不合法，以及政策政策测试
			console.log('\x1b[35m不存在、不合法，以及政策政策测试 ...\x1b[0m\n');
			await startModule('PolicyQueryMessage', async type => {
				success(await POST({ type, place: 2 }), null);
				success(await POST({ type, place: 3 }), policies[1]);
				success(await POST({ type, place: 4 }), policies[1]);
				success(await POST({ type, place: 5 }), null);
				success(await POST({ type, place: 6 }), null);
				success(await POST({ type, place: 7 }), null);
				success(await POST({ type, place: 8 }), null);
				success(await POST({ type, place: 9 }), null);
				success(await POST({ type, place: 10 }), null);
				success(await POST({ type, place: 11 }), null);
				success(await POST({ type, place: 12 }), null);
				success(await POST({ type, place: 13 }), null);

				success(await POST(
					{ type: 'PolicyUpdateMessage', userToken: rootToken, place: 2, content: policies[2] }
				));
				success(await POST(
					{ type: 'PolicyUpdateMessage', userToken: rootToken, place: 1, content: policies[3] }
				));

				success(await POST({ type, place: 2 }), policies[2]);
				success(await POST({ type, place: 3 }), policies[1]);
				success(await POST({ type, place: 4 }), policies[1]);
				success(await POST({ type, place: 5 }), policies[2]);
				success(await POST({ type, place: 6 }), policies[2]);
				success(await POST({ type, place: 7 }), policies[2]);
				success(await POST({ type, place: 8 }), policies[2]);
				success(await POST({ type, place: 9 }), policies[3]);
				success(await POST({ type, place: 10 }), policies[3]);
				success(await POST({ type, place: 11 }), policies[3]);
				success(await POST({ type, place: 12 }), null);
				success(await POST({ type, place: 13 }), null);

				success(await POST(
					{ type: 'PolicyUpdateMessage', userToken: rootToken, place: 3, content: '' }
				));
				success(await POST(
					{ type: 'PolicyUpdateMessage', userToken: rootToken, place: 2, content: '' }
				));
				success(await POST(
					{ type: 'PolicyUpdateMessage', userToken: rootToken, place: 1, content: '' }
				));

				success(await POST({ type, place: 2 }), null);
				success(await POST({ type, place: 3 }), null);
				success(await POST({ type, place: 4 }), null);
				success(await POST({ type, place: 5 }), null);
				success(await POST({ type, place: 6 }), null);
				success(await POST({ type, place: 7 }), null);
				success(await POST({ type, place: 8 }), null);
				success(await POST({ type, place: 9 }), null);
				success(await POST({ type, place: 10 }), null);
				success(await POST({ type, place: 11 }), null);
				success(await POST({ type, place: 12 }), null);
				success(await POST({ type, place: 13 }), null);
			}, 0);
		}

		{ // 风险区测试 (code.`DangerousPlaceMessage` / `SetDangerousPlaceMessage`)
			// 风险区修改和查询
			for (let t = 0; t < 3; ++t) {
				console.log(`\x1b[35m风险区修改第 \x1b[32m${t + 1}/3\x1b[35m 轮 ...\x1b[0m`);
				await startModule('SetDangerousPlaceMessage', async type => {
					const data = { type, userToken, place: 3, level: t };
					error(await POST(data), ERRORS.NO_PERMISSION);

					data.userToken = rootToken;
					success(await POST(data));
				});

				await startModule('DangerousPlaceMessage', async type => {
					const data = { type, place: 3 };
					success(await POST(data), t);
				});
			}
		}

		{ // 管理权限更改测试 (user.admin)
			const permission = { userName: 'cat', setRiskAreas: true };
			await startModule('GetAdminPermissionMessage / SetAdminPermissionMessage', async () => {
				const
					dataGet = { type: 'GetAdminPermissionMessage', userToken },
					dataSet = { type: 'SetAdminPermissionMessage', userToken, permission };
				error(await POST(dataSet), ERRORS.NO_PERMISSION);

				dataSet.userToken = rootToken;
				success(await POST(dataSet));

				const result1 = success(await POST(dataGet), _);
				assert(Object.entries(result1).every(([key, value]) => 'setRiskAreas' === key === value || key === 'userName'));

				{
					const data1 = { type: 'SetDangerousPlaceMessage', userToken, place: 3, level: 2 };
					success(await POST(data1));

					const data2 = { type: 'DangerousPlaceMessage', place: 3 };
					success(await POST(data2), 2);

					const data3 = { type: 'PolicyUpdateMessage', userToken, place: 3, content: '改不了的' };
					error(await POST(data3), ERRORS.NO_PERMISSION);
				}

				dataSet.permission = { userName: 'cat', setPolicy: true };
				success(await POST(dataSet));

				const result2 = success(await POST(dataGet), _);
				assert(Object.entries(result2).every(([key, value]) => 'setPolicy' === key === value || key === 'userName'));

				{
					const data1 = { type: 'PolicyUpdateMessage', userToken, place: 3, content: '卷宽太坏了' };
					success(await POST(data1));

					const data2 = { type: 'PolicyQueryMessage', place: 3 };
					success(await POST(data2), '卷宽太坏了');

					const data3 = { type: 'SetDangerousPlaceMessage', userToken, place: 3, level: 1 };
					error(await POST(data3), ERRORS.NO_PERMISSION);
				}

				dataSet.permission = { userName: 'cat' };
				success(await POST(dataSet));

				const result3 = success(await POST(dataGet), _);
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
				error(result, _);

				data.reason = '我还要抱宽宽！';
				error(await POST(data), _);
			});

			// 查询申诉
			await startModule('QueryAppealMessage', async type => {
				const data = { type, userToken, idCard };
				error(await POST(data), ERRORS.NO_PERMISSION);

				success(await POST({
					type: 'SetAdminPermissionMessage',
					userToken: rootToken,
					permission: { userName: 'cat', viewAppeals: true }
				}));

				const result1 = success(await POST(data), _);
				assert.eq(result1.idCard, idCard);
				assert.eq(result1.reason, '我要抱猫猫！');
				time(result1.time);

				data.idCard = hzkIdCard;
				success(await POST(data), null);

				success(await POST({
					type: 'SetAdminPermissionMessage',
					userToken: rootToken,
					permission: { userName: 'cat' }
				}));

				data.idCard = idCard;
				error(await POST(data), ERRORS.NO_PERMISSION);
			});

			// 解决申诉
			await startModule('ResolveAppealMessage', async type => {
				const data = { type, userToken: rootToken, idCard };
				success(await POST(data));

				success(await POST(data), 0);

				success(await POST({ type: 'QueryAppealMessage', userToken: rootToken, idCard }), null);
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
				error(result, _);

				data.reason = '宽宽抱起来不舒服！';
				error(await POST(data), _);
			});
		}

		{ // 权限授予测试 (user.permission)
			// 权限授予
			await startModule('UserGrantPermissionMessage', async type => {
				const data = { type: 'UserAppealMessage', userToken, idCard: hzkIdCard, reason: '没有权限的猫猫~' };
				error(await POST(data), `错误！无权限访问 (或不存在) 身份证号为 ${hzkIdCard} 的用户！`);

				success(await POST({ type, userToken: hzkToken, other: 'cat' }));

				data.reason = '有权限的猫猫！';
				success(await POST(data), 1);

				const result = success(
					await POST({ type: 'QueryAppealMessage', userToken: rootToken, idCard: hzkIdCard }), _
				);

				assert.eq(result.idCard, hzkIdCard);
				assert.eq(result.reason, '有权限的猫猫！');
				time(result.time);

				success(await POST({ type: 'ResolveAppealMessage', userToken: rootToken, idCard: hzkIdCard }));
			});

			// 权限查看
			await startModule('UserFetchAllGrantedUsersMessage', async type => {
				const data = { type, userToken: hzkToken };
				success(await POST(data), ['cat']);
			});

			// 权限撤销
			await startModule('UserRevokePermissionMessage', async type => {
				const data = { type: 'UserAppealMessage', userToken, idCard: hzkIdCard, reason: '没有权限的猫猫~' };

				success(await POST({ type, userToken: hzkToken, other: 'cat' }));

				error(await POST(data), `错误！无权限访问 (或不存在) 身份证号为 ${hzkIdCard} 的用户！`);

				success(await POST({ type: 'UserFetchAllGrantedUsersMessage', userToken: hzkToken }), []);
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
				vaccines = success(await POST(data), _);
				assert(Array.isArray(vaccines));
			});

			vaccines.push(
				{ idCard, manufacture: manufacture + ' A', time: time1, vaccineType: vaccines.length + 1 },
				{ idCard, manufacture: manufacture + ' B', time: time2, vaccineType: vaccines.length + 2 },
			);
			// 添加疫苗
			await startModule('UserAddVaccineMessage', async type => {
				const data = { type, userToken, idCard };
				success(await POST({ ...data, manufacture: manufacture + ' A', time: time1 }));
				success(await POST({ ...data, manufacture: manufacture + ' B', time: time2 }));
				success(await POST({ type: 'UserGetVaccineMessage', userToken, idCard }), vaccines);
			});
		}

		{ // 核酸测试 (nucleicAcidTest)
			const name = stringUtil.randomString(20);
			console.log(`\x1b[35m增加核酸测试点：\x1b[32m${name}\x1b[0m\n`);

			// 增加核酸测试点
			await startModule('AddNucleicAcidTestPointMessage', async type => {
				const data = { type, userToken: rootToken, place: 4, name };
				error(
					await POST({ ...data, name: '@<-这是非法字符' }),
					'核酸测试点名称 @<-这是非法字符 不合法！'
				);
				success(await POST(data));
				error(await POST(data), _);
			});

			// 获取核酸测试点
			await startModule('GetAllNucleicAcidTestPointMessage', async type => {
				const data = { type, place: 4 };
				const result = success(await POST(data), _);
				assert(result.some(w => eq(w, { place: 4, name })));
			});

			// 预约核酸
			await startModule('AppointNucleicAcidTestMessage', async type => {
				const data = { type, userToken, idCard, testPlace: name };
				error(
					await POST({ ...data, testPlace: '这个地点应当不存在' }),
					'核酸测试点 这个地点应当不存在 不存在！'
				);
				success(await POST(data));
				error(await POST(data), `错误！身份证号为 ${idCard} 的核酸预约已存在`)
			});

			// 查询核酸预约点排队人数
			await startModule('QueryTestPointWaitingPersonMessage', async type => {
				const data = { type, place: name };
				success(await POST(data), 1);
			});

			// 查询核酸预约点所有人
			await startModule('AdminQueryTestPointWaitingPersonMessage', async type => {
				const data = { type, userToken, place: name };
				error(await POST(data), ERRORS.NO_PERMISSION);

				data.userToken = rootToken;
				const result = success(await POST(data), _);
				assert.eq(result.length, 1);
				assert.eq(result[0].idCard, idCard);
				assert.eq(result[0].testPlace, name);
				time(result[0].appointTime);
			});

			// 完成核酸
			await startModule('FinishNucleicAcidTestMessage', async type => {
				const data = { type, userToken, idCard, testPlace: name, nucleicResult: true };
				error(await POST(data), ERRORS.NO_PERMISSION);

				data.userToken = rootToken;
				success(await POST(data));
				error(await POST(data), `错误！身份证号为 ${idCard} 的用户未进行预约`)
			});

			// 获取核酸测试结果
			await startModule('GetNucleicAcidTestResultsMessage', async type => {
				const data = { type, userToken, idCard };
				const result = success(await POST(data), _).filter(r => r.testPlace === name);
				assert.eq(result.length, 1);
				assert.eq(result[0].idCard, idCard);
				assert.eq(result[0].result, true);
				time(result[0].time);
			});
		}

		{ // 健康码颜色测试 (code.`UserGetColorMessage` / `AdminSetColorMessage`)
			await startModule('UserGetColorMessage', async type => {
				const
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
					dataRisk = { type: 'SetDangerousPlaceMessage', userToken: rootToken };
				// 默认绿码
				success(await POST(dataGet), 0);
				// 添加轨迹
				success(await POST({ ...traceAdd, trace: 3 }));
				// 设置低风险区
				success(await POST({ ...dataRisk, place: 3, level: 0 }));
				// 仍为绿码
				success(await POST(dataGet), 0);
				// 设置中风险区
				success(await POST({ ...dataRisk, place: 3, level: 1 }));
				// 为黄码
				success(await POST(dataGet), 2);
				// 设置高风险区
				success(await POST({ ...dataRisk, place: 3, level: 2 }));
				// 为红码
				success(await POST(dataGet), 3);
				// 设置中风险区
				success(await POST({ ...dataRisk, place: 3, level: 1 }));
				// 仍为红码
				success(await POST(dataGet), 3);
				// 查询轨迹
				const traces1 = success(await POST(traceGet), _)[0];
				// 移除轨迹
				success(await POST({ ...traceDelete, time: traces1.time }));
				// 仍为红码
				success(await POST(dataGet), 3);
				// 管理员赋绿码
				success(await POST({ ...dataSet, color: 0 }), 1);
				// 为绿码
				success(await POST(dataGet), 0);
				// 加回轨迹 (中风险)
				success(await POST({ ...traceAdd, trace: 3 }));
				// 为黄码
				success(await POST(dataGet), 2);

				// hzk 为绿码
				success(await POST(dataGetHzk), 0);
				// hzk 增加密接
				success(await POST({ ...ccAddHzk, cc: 'cat' }));
				// hzk 为弹窗
				success(await POST(dataGetHzk), 1);
				// hzk 去中风险地区
				success(await POST({ ...traceAddHzk, trace: 3 }));
				// hzk 为黄码
				success(await POST(dataGetHzk), 2);

				// hzk 移除轨迹
				const traces2 = success(await POST(traceGetHzk), _)[0];
				success(await POST({ ...traceDeleteHzk, time: traces2.time }));
				const traces3 = success(await POST(ccGetHzk), _)[0];
				success(await POST({ ...ccDeleteHzk, time: traces3.time }));
				// 管理员赋绿码
				success(await POST({ ...dataSetHzk, color: 0 }), 1);
				// 为绿码
				success(await POST(dataGetHzk), 0);

				// 添加轨迹
				success(await POST({ ...traceAdd, trace: 5 }));
				// 为黄码
				success(await POST(dataGet), 2);
				// 设置低风险地区
				success(await POST({ ...dataRisk, place: 5, level: 0 }));
				// 为黄码
				success(await POST(dataGet), 2);
				// 设置高风险地区
				success(await POST({ ...dataRisk, place: 5, level: 2 }));
				// 为红码
				success(await POST(dataGet), 3);

				// hzk 为绿码
				success(await POST(dataGetHzk), 0);
				// hzk 增加密接
				success(await POST({ ...ccAddHzk, cc: 'cat' }));
				// hzk 为黄码
				success(await POST(dataGetHzk), 2);

				// 重置低风险
				success(await POST({ ...dataRisk, place: 5, level: 0 }));

				// 移除轨迹
				const traces4 = success(await POST(traceGet), _);
				assert.eq(traces4.length, 2);
				success(await POST({ ...traceDelete, time: traces4[0].time }));
				success(await POST({ ...traceDelete, time: traces4[1].time }));
				const traces5 = success(await POST(ccGetHzk), _);
				success(await POST({ ...ccDeleteHzk, time: traces5[0].time }));

				// 管理员赋绿码
				success(await POST({ ...dataSet, color: 0 }), 1);
				// 为绿码
				success(await POST(dataGet), 0);
				// 管理员赋绿码
				success(await POST({ ...dataSetHzk, color: 0 }), 1);
				// 为绿码
				success(await POST(dataGetHzk), 0);
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
	success(await POST(data));
});

*/
