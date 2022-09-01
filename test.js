#!/usr/bin/env node

const
	assert = require('assert'),
	exit = () => process.exit(0),
	httpRequest = require('http').request,
	httpsRequest = require('https').request;

const APIConfig = {
	method: 'POST',
	headers: { 'content-type': 'application/json; charset=utf-8' },
	hostname: 'localhost',
	port: 6070,
	path: '/api',
	protocol: 'http:'
};

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
	assert.equal(result.status, 0);
	assert.equal(result.message, value);
}

function assertError(result, value) {
	assert.equal(result.status, -1);
	assert.equal(result.message, value);
}

async function test() {
	try {
		const
			idCard = '00000000000000001x', hzkIdCard = '00000000000000028x',
			place = { province: '卷猫', city: '猫猫', county: '真猫' };
		let userToken = '', rootToken = '', hzkToken = '';

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
				const result2 = await POST(data);
				assert.equal(result2.status, -1);

				data.idCard = hzkIdCard.toUpperCase();
				await POST(data);

				/********/ console.log('\x1b[35mroot 用户 ...\x1b[0m'); /********/

				data.userName = 'root', data.password = '123456', data.realName = '管理员', data.idCard = idCard;
				const result3 = await POST(data);
				assert.equal(result3.status, -1);

				data.idCard = '400000202101081832';
				await POST(data);
			});

			// 登录
			await startModule('UserLoginMessage', async type => {
				const data = { type, userName: 'cat', password: 'lszzsxn' };

				/********/ console.log('\x1b[35m普通用户 ...\x1b[0m'); /********/

				const result1 = await POST(data);
				assert.equal(result1.status, 0);
				userToken = result1.message;
				assert.equal(typeof userToken, 'string');

				data.userName = 'wide', data.password = 'hzkhzk';
				const result2 = await POST(data);
				assert.equal(result2.status, 0);
				hzkToken = result2.message;
				assert.equal(typeof hzkToken, 'string');

				/********/ console.log('\x1b[35mroot 用户 ...\x1b[0m'); /********/

				data.userName = 'root', data.password = '123456';
				const result3 = await POST(data);
				assert.equal(result3.status, 0);
				rootToken = result3.message;
				assert.equal(typeof rootToken, 'string');
			});

			// 用户信息查询
			await startModule('UserGetProfileMessage', async type => {
				const data = { type, userToken };
				const result1 = await POST(data);
				assert.equal(result1.status, 0);
				assert.equal(result1.message.userName, 'cat');
				assert.equal(result1.message.password, 'lszzsxn');
				assert.equal(result1.message.realName, '猫猫');
				assert.equal(result1.message.idCard, idCard);

				data.userToken = rootToken;
				const result2 = await POST(data);
				assert.equal(result2.status, 0);
				assert.equal(result2.message.userName, 'root');
				assert.equal(result2.message.password, '123456');
				assert.equal(result2.message.realName, '管理员');
				assert.equal(result2.message.idCard, '400000202101081832');

				data.userToken = '不存在的';
				const result3 = await POST(data);
				assertError(result3, '错误！用户不存在或登录信息已过期！');
			});
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
				const result = await POST(data);
				assert.equal(result.status, 0);
				traces = result.message;
				assert(Array.isArray(traces) && traces.length === 3);
			});

			// 更新轨迹
			await startModule('UserUpdateTraceMessage', async type => {
				const data = {
					type, userToken, idCard, time: 233,
					trace: { province: '省 4', city: '市 4', county: '区 4' }
				};
				const result1 = await POST(data);
				assert.equal(result1.status, 0);
				assert.equal(result1.message, 0);

				data.time = traces.at(-1).time;
				data.trace = { province: '省 5', city: '市 5', county: '区 5' };
				const result2 = await POST(data);
				assert.equal(result2.status, 0);
				assert.equal(result2.message, 1);
			});

			// 删除轨迹
			await startModule('UserDeleteTraceMessage', async type => {
				const data = { type, userToken, idCard, time: 233, };
				const result1 = await POST(data);
				assert.equal(result1.status, 0);
				assert.equal(result1.message, 0);

				data.time = traces.at(-2).time;
				const result2 = await POST(data);
				assert.equal(result2.status, 0);
				assert.equal(result2.message, 1);
				traces.splice(-2, 1);
			});

			console.log('\x1b[35m检查是否成功删除 ...\x1b[0m\n');
			await startModule('UserGetTraceMessage', async type => {
				const data = { type, userToken, idCard, startTime: 0, endTime: 1e18 };
				const result = await POST(data);
				assert.equal(result.status, 0);
				assert(Array.isArray(result.message) && result.message.length === traces.length);
			}, 0);

			console.log('\x1b[35m删除剩余全部数据 ...\x1b[0m\n');
			await startModule('UserDeleteTraceMessage', async type => {
				await Promise.all(
					traces.map(async trace => {
						const data = { type, userToken, idCard, time: trace.time };
						const result = await POST(data);
						assert.equal(result.status, 0);
						assert.equal(result.message, 1);
					})
				);
			}, 0);
		}

		{ // 贴贴轨迹测试 (trace.withPeople)
			// 上报密接
			await startModule('UserAddTraceWithPeopleMessage', async type => {
				const data = { type, userToken, idCard, personIdCard: '012345678901234560' };
				assertSuccess(await POST(data));
			});

			// 获取轨迹
			let traces = [];
			await startModule('UserGetTraceWithPeopleMessage', async type => {
				const data = { type, userToken, idCard, startTime: 0, endTime: 1e18 };
				const result = await POST(data);
				assert.equal(result.status, 0);
				traces = result.message;
				assert(Array.isArray(traces));
			});
		}

		{ // 政策测试 (policy)
			// 政策修改和查询
			const policies = ['卷宽卷宽喵希喵希！', '猫猫和真猫贴贴！'];

			for (let t = 0; t < 2; ++t) {
				console.log(`\x1b[35m政策修改第 \x1b[32m${t + 1}/2\x1b[35m 轮 ...\x1b[0m`);
				await startModule('PolicyUpdateMessage', async type => {
					const data = { type, userToken, place, content: policies[t] };
					assertError(await POST(data), '错误！没有权限进行此操作');

					data.userToken = rootToken;
					assertSuccess(await POST(data));
				});

				await startModule('PolicyQueryMessage', async type => {
					const data = { type, place };
					const result = await POST(data);
					assert.equal(result.status, 0);
					assert.equal(result.message, policies[t]);
				});
			}

			console.log('\x1b[35m不存在以及不合法政策测试 ...\x1b[0m\n');
			await startModule('PolicyQueryMessage', async type => {
				const data = { type, place: { province: '不存在的', city: '坏了', county: '没了' } };
				assertSuccess(await POST(data), null);

				data.place = { '猫猫': '宽宽' };
				try {
					await POST(data);
					throw new Error();
				} catch (e) {
					assert.equal(e.message, 'Unexpected token U in JSON at position 0');
				}
			}, 0);

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

		{ // 风险区测试 (dangerousPlace)
			// 风险区修改和查询
			for (let t = 0; t < 3; ++t) {
				console.log(`\x1b[35m风险区修改第 \x1b[32m${t + 1}/3\x1b[35m 轮 ...\x1b[0m`);
				await startModule('SetDangerousPlaceMessage', async type => {
					const data = { type, userToken, place, level: t };
					assertError(await POST(data), '错误！没有权限进行此操作');

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
				const result = await POST(data);
				assert.equal(result.status, -1);

				data.place = { '猫猫': '宽宽' };
				try {
					await POST(data);
					throw new Error();
				} catch (e) {
					assert.equal(e.message, 'Unexpected token U in JSON at position 0');
				}
			}, 0);
		}

		{ // 管理权限更改测试 (user.admin)
			const permission = { userName: 'cat', setRiskAreas: true };
			await startModule('GetAdminPermissionMessage / SetAdminPermissionMessage', async () => {
				const
					dataGet = { type: 'GetAdminPermissionMessage', userToken },
					dataSet = { type: 'SetAdminPermissionMessage', userToken, permission };
				assertError(await POST(dataSet), '错误！没有权限进行此操作');

				dataSet.userToken = rootToken;
				assertSuccess(await POST(dataSet));

				const result1 = await POST(dataGet);
				assert.equal(result1.status, 0);
				assert(Object.entries(result1.message).every(([key, value]) => 'setRiskAreas' === key === value || key === 'userName'));

				{
					const data1 = { type: 'SetDangerousPlaceMessage', userToken, place, level: 2 };
					assertSuccess(await POST(data1));

					const data2 = { type: 'DangerousPlaceMessage', place };
					assertSuccess(await POST(data2), 2);

					const data3 = { type: 'PolicyUpdateMessage', userToken, place, content: '改不了的' };
					assertError(await POST(data3), '错误！没有权限进行此操作');
				}

				dataSet.permission = { userName: 'cat', setPolicy: true };
				assertSuccess(await POST(dataSet));

				const result2 = await POST(dataGet);
				assert.equal(result2.status, 0);
				assert(Object.entries(result2.message).every(([key, value]) => 'setPolicy' === key === value || key === 'userName'));

				{
					const data1 = { type: 'PolicyUpdateMessage', userToken, place, content: '卷宽太坏了' };
					assertSuccess(await POST(data1));

					const data2 = { type: 'PolicyQueryMessage', place };
					assertSuccess(await POST(data2), '卷宽太坏了');

					const data3 = { type: 'SetDangerousPlaceMessage', userToken, place, level: 1 };
					assertError(await POST(data3), '错误！没有权限进行此操作');
				}

				dataSet.permission = { userName: 'cat' };
				assertSuccess(await POST(dataSet));

				const result3 = await POST(dataGet);
				assert.equal(result3.status, 0);
				assert(Object.entries(result3.message).every(([key, value]) => value === false || key === 'userName'));
			});
		}

		{ // 申诉测试 (code.appeal)
			// 添加状态申诉
			await startModule('UserAppealMessage', async type => {
				const data = { type, userToken, idCard, reason: '我要抱猫猫！' };
				let result = await POST(data);
				if (result.status === 0) {
					assert.equal(result.message, 1);
					result = await POST(data);
				}
				assert.equal(result.status, -1);
				// assert(result.message.includes('duplicate'));

				data.reason = '我还要抱宽宽！';
				result = await POST(data);
				assert.equal(result.status, -1);
				// assert(result.message.includes('duplicate'));
			});

			// 查询申诉
			await startModule('QueryAppealMessage', async type => {
				const data = { type, userToken, idCard };
				assertError(await POST(data), '错误！没有权限进行此操作');

				assertSuccess(await POST({
					type: 'SetAdminPermissionMessage',
					userToken: rootToken,
					permission: { userName: 'cat', viewAppeals: true }
				}));

				const result1 = await POST(data);
				assert.equal(result1.status, 0);
				assert.equal(result1.message.idCard, idCard);
				assert.equal(result1.message.reason, '我要抱猫猫！');
				assert.equal(typeof result1.message.time, 'number');

				data.idCard = hzkIdCard;
				assertSuccess(await POST(data), null);

				assertSuccess(await POST({
					type: 'SetAdminPermissionMessage',
					userToken: rootToken,
					permission: { userName: 'cat' }
				}));

				data.idCard = idCard;
				assertError(await POST(data), '错误！没有权限进行此操作');
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
					assert.equal(result.message, 1);
					result = await POST(data);
				}
				assert.equal(result.status, -1);
				// assert(result.message.includes('duplicate'));

				data.reason = '宽宽抱起来不舒服！';
				result = await POST(data);
				assert.equal(result.status, -1);
				// assert(result.message.includes('duplicate'));
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

				const result = await POST({ type: 'QueryAppealMessage', userToken: rootToken, idCard: hzkIdCard });

				assert.equal(result.status, 0);
				assert.equal(result.message.idCard, hzkIdCard);
				assert.equal(result.message.reason, '有权限的猫猫！');
				assert.equal(typeof result.message.time, 'number');

				assertSuccess(await POST({ type: 'ResolveAppealMessage', userToken: rootToken, idCard: hzkIdCard }));
			});

			// 权限查看
			await startModule('UserFetchAllGrantedUsersMessage', async type => {
				const result = await POST({ type, userToken: hzkToken });
				assert.equal(result.status, 0);
				assert(
					Array.isArray(result.message) &&
					result.message.length === 1 &&
					result.message[0] === 'cat'
				);
			});

			// 权限撤销
			await startModule('UserRevokePermissionMessage', async type => {
				const data = { type: 'UserAppealMessage', userToken, idCard: hzkIdCard, reason: '没有权限的猫猫~' };

				assertSuccess(await POST({ type, userToken: hzkToken, other: 'cat' }));

				assertError(await POST(data), `错误！无权限访问 (或不存在) 身份证号为 ${hzkIdCard} 的用户！`);

				const result = await POST({ type: 'UserFetchAllGrantedUsersMessage', userToken: hzkToken });
				assert.equal(result.status, 0);
				assert(
					Array.isArray(result.message) &&
					result.message.length === 0
				);
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
	const result = await POST(data);
	assert.equal(result.status, 0);
	assert.equal(result.message, 1);
});

*/
