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

async function startModule(type, test, log = true) {
	if (log) console.log(`\x1b[33m测试 \x1b[36m${type} \x1b[33m中 \x1b[0m...`);
	await test(type);
	console.log(`\x1b[36m${type} \x1b[32m测试通过！\x1b[0m\n`);
}

async function test() {
	try {
		const
			idCard = '2021010818', hzkIdCard = '2021010698',
			place = { province: '卷猫', city: '猫猫', county: '真猫' };
		let userToken = '', rootToken = '';

		{ // 登录/注册测试
			// 注册以及用户已存在
			await startModule('UserRegisterMessage', async type => {
				const data = { type, userName: 'cat', password: 'lsz', realName: '猫猫', idCard };
				let result = await POST(data);
				if (result.status === 0) result = await POST(data);
				assert.equal(result.status, -1);
				assert.equal(result.message, '错误！用户名已经存在了');

				await POST({ type, userName: 'wide', password: 'hzk', realName: '卷宽', idCard: hzkIdCard });
			});

			// 登录
			await startModule('UserLoginMessage', async type => {
				const data = { type, userName: 'cat', password: 'lsz' };
				const result = await POST(data);
				assert.equal(result.status, 0);
				userToken = result.message;
				assert.equal(typeof userToken, 'string');
			});

			// root 用户注册
			console.log('\x1b[35mroot 用户测试 ...\x1b[0m');
			await startModule('UserRegisterMessage', async type => {
				const data = { type, userName: 'root', password: '123456', realName: '管理员', idCard };
				const result = await POST(data);
				assert.equal(result.status, -1);

				data.idCard = 'root';
				await POST(data);
			}, false);

			// root 用户登录
			await startModule('UserLoginMessage', async type => {
				const data = { type, userName: 'root', password: '123456' };
				const result = await POST(data);
				assert.equal(result.status, 0);
				rootToken = result.message;
				assert.equal(typeof rootToken, 'string');
			}, false);
		}

		{ // 轨迹测试
			// 增加轨迹
			await startModule('UserAddTraceMessage', async type => {
				const data = { type, userToken: '无效 token', idCard, trace: place };
				const result1 = await POST(data);
				assert.equal(result1.status, -1);
				assert.equal(result1.message, '错误！用户不存在或登录信息已过期！');

				data.userToken = userToken;
				data.idCard = 'hahaha';
				const result2 = await POST(data);
				assert.equal(result2.status, -1);
				assert.equal(result2.message, '错误！不存在身份证号为 IDCard(hahaha) 的用户！');

				data.idCard = hzkIdCard;
				const result3 = await POST(data);
				assert.equal(result3.status, -1);
				assert.equal(result3.message, `错误！无权限访问身份证号为 IDCard(${hzkIdCard}) 的用户！`);

				data.idCard = idCard;
				const result4 = await POST(data);
				assert.equal(result4.status, 0);
				assert.equal(result4.message, 1);

				data.trace = { province: '省 2', city: '市 2', county: '区 2' };
				const result5 = await POST(data);
				assert.equal(result5.status, 0);
				assert.equal(result5.message, 1);

				data.trace = { province: '省 3', city: '市 3', county: '区 3' };
				const result6 = await POST(data);
				assert.equal(result6.status, 0);
				assert.equal(result6.message, 1);
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

			console.log('\x1b[35m检查是否成功删除 ...\x1b[0m');
			await startModule('UserGetTraceMessage', async type => {
				const data = { type, userToken, idCard, startTime: 0, endTime: 1e18 };
				const result = await POST(data);
				assert.equal(result.status, 0);
				assert(Array.isArray(result.message) && result.message.length === traces.length);
			}, false);

			console.log('\x1b[35m删除剩余全部数据 ...\x1b[0m');
			await startModule('UserDeleteTraceMessage', async type => {
				await Promise.all(
					traces.map(async trace => {
						const data = { type, userToken, idCard, time: trace.time };
						const result = await POST(data);
						assert.equal(result.status, 0);
						assert.equal(result.message, 1);
					})
				);
			}, false);
		}

		// 上报密接
		await startModule('UserAddTraceWithPeopleMessage', async type => {
			const data = { type, userToken, idCard, personIdCard: 2021011832 };
			const result = await POST(data);
			assert.equal(result.status, 0);
			assert.equal(result.message, 1);
		});

		{ // 政策测试
			// 政策修改和查询
			const policies = ['卷宽卷宽喵希喵希！', '猫猫和真猫贴贴！'];

			for (let t = 0; t < 2; ++t) {
				console.log(`\x1b[35m政策修改第 \x1b[32m${t + 1}/2\x1b[35m 轮 ...\x1b[0m`);
				await startModule('PolicyUpdateMessage', async type => {
					const data = { type, userToken, place, content: policies[t] };
					const result1 = await POST(data);
					assert.equal(result1.status, -1);
					assert.equal(result1.message, '错误！没有权限进行此操作');

					data.userToken = rootToken;
					const result2 = await POST(data);
					assert.equal(result2.status, 0);
					assert.equal(result2.message, 1);
				});

				await startModule('PolicyQueryMessage', async type => {
					const data = { type, place };
					const result = await POST(data);
					assert.equal(result.status, 0);
					assert.equal(result.message, policies[t]);
				});
			}

			console.log('\x1b[35m不存在以及不合法政策测试 ...\x1b[0m');
			await startModule('PolicyQueryMessage', async type => {
				const data = { type, place: { province: '不存在的', city: '坏了', county: '没了' } };
				const result = await POST(data);
				assert.equal(result.status, 0);
				assert.equal(result.message, null);

				data.place = { '猫猫': '宽宽' };
				try {
					await POST(data);
					throw new Error();
				} catch (e) {
					assert.equal(e.message, 'Unexpected token U in JSON at position 0');
				}
			}, false);

			console.log('\x1b[1;35m政策继承测试 ...\x1b[0m');
			await startModule('PolicyUpdateMessage', async type => {
				const data = { type, userToken: rootToken, place: { province: '卷猫', city: '', county: '' }, content: '猫宽学！' };
				const result1 = await POST(data);
				assert.equal(result1.status, 0);
				assert.equal(result1.message, 1);

				const qdata = { type: 'PolicyQueryMessage', place };
				const result2 = await POST(qdata);
				assert.equal(result2.status, 0);
				assert.equal(result2.message, policies[1]);

				qdata.place = { ...place, county: '假猫' };
				const result3 = await POST(qdata);
				assert.equal(result3.status, 0);
				assert.equal(result3.message, '猫宽学！');

				data.content = '';
				const result4 = await POST(data);
				assert.equal(result4.status, 0);
				assert.equal(result4.message, 1);

				qdata.place.county = '真猫';
				const result5 = await POST(qdata);
				assert.equal(result5.status, 0);
				assert.equal(result5.message, policies[1]);

				qdata.place.county = '假猫';
				const result6 = await POST(qdata);
				assert.equal(result6.status, 0);
				assert.equal(result6.message, null);
			}, false);
		}

		{ // 风险区测试
			// 风险区修改和查询
			for (let t = 0; t < 3; ++t) {
				console.log(`\x1b[35m风险区修改第 \x1b[32m${t + 1}/3\x1b[35m 轮 ...\x1b[0m`);
				await startModule('SetDangerousPlaceMessage', async type => {
					const data = { type, userToken, place, level: t };
					const result1 = await POST(data);
					assert.equal(result1.status, -1);
					assert.equal(result1.message, '错误！没有权限进行此操作');

					data.userToken = rootToken;
					const result2 = await POST(data);
					assert.equal(result2.status, 0);
					assert.equal(result2.message, 1);
				});

				await startModule('DangerousPlaceMessage', async type => {
					const data = { type, place };
					const result = await POST(data);
					assert.equal(result.status, 0);
					assert.equal(result.message, t);
				});
			}

			console.log('\x1b[35m不存在以及不合法地区测试 ...\x1b[0m');
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
			}, false);
		}

		{ // 权限更改测试
			const permission = { userName: 'cat', setRiskAreas: true };
			await startModule('SetPermissionMessage', async type => {
				const data = { type, userToken, permission };
				const result1 = await POST(data);
				assert.equal(result1.status, -1);
				assert.equal(result1.message, '错误！没有权限进行此操作');

				data.userToken = rootToken;
				const result2 = await POST(data);
				assert.equal(result2.status, 0);
				assert.equal(result2.message, 1);

				{
					const data1 = { type: 'SetDangerousPlaceMessage', userToken, place, level: 2 };
					const result1 = await POST(data1);
					assert.equal(result1.status, 0);
					assert.equal(result1.message, 1);

					const data2 = { type: 'DangerousPlaceMessage', place };
					const result2 = await POST(data2);
					assert.equal(result2.status, 0);
					assert.equal(result2.message, 2);

					const data3 = { type: 'PolicyUpdateMessage', userToken, place, content: '改不了的' };
					const result3 = await POST(data3);
					assert.equal(result3.status, -1);
					assert.equal(result3.message, '错误！没有权限进行此操作');
				}

				data.permission = { userName: 'cat', setPolicy: true };
				const result3 = await POST(data);
				assert.equal(result3.status, 0);
				assert.equal(result3.message, 1);

				{
					const data1 = { type: 'PolicyUpdateMessage', userToken, place, content: '卷宽太坏了' };
					const result1 = await POST(data1);
					assert.equal(result1.status, 0);
					assert.equal(result1.message, 1);

					const data2 = { type: 'PolicyQueryMessage', place };
					const result2 = await POST(data2);
					assert.equal(result2.status, 0);
					assert.equal(result2.message, '卷宽太坏了');

					const data3 = { type: 'SetDangerousPlaceMessage', userToken, place, level: 1 };
					const result3 = await POST(data3);
					assert.equal(result3.status, -1);
					assert.equal(result3.message, '错误！没有权限进行此操作');
				}

				data.permission = { userName: 'cat' };
				const result4 = await POST(data);
				assert.equal(result4.status, 0);
				assert.equal(result4.message, 1);
			});
		}

		{ // 申诉测试
			let time = 0;
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
				const result1 = await POST(data);
				assert.equal(result1.status, -1);
				assert.equal(result1.message, '错误！没有权限进行此操作');

				const result2 = await POST(
					{ type: 'SetPermissionMessage', userToken: rootToken, permission: { userName: 'cat', viewAppeals: true } }
				);
				assert.equal(result2.status, 0);
				assert.equal(result2.message, 1);

				const result3 = await POST(data);
				assert.equal(result3.status, 0);
				assert.equal(result3.message.idCard, idCard);
				assert.equal(result3.message.reason, '我要抱猫猫！');
				({ time } = result3.message);

				data.idCard = +hzkIdCard + 1;
				const result4 = await POST(data);
				assert.equal(result4.status, 0);
				assert.equal(result4.message, null);

				const result5 = await POST(
					{ type: 'SetPermissionMessage', userToken: rootToken, permission: { userName: 'cat' } }
				);
				assert.equal(result5.status, 0);
				assert.equal(result5.message, 1);

				data.idCard = idCard;
				const result6 = await POST(data);
				assert.equal(result6.status, -1);
				assert.equal(result6.message, '错误！没有权限进行此操作');
			});

			// 解决申诉
			await startModule('ResolveAppealMessage', async type => {
				const data = { type, userToken: rootToken, idCard };
				const result1 = await POST(data);
				assert.equal(result1.status, 0);
				assert.equal(result1.message, 1);

				const result2 = await POST(data);
				assert.equal(result2.status, 0);
				assert.equal(result2.message, 0);

				const result3 = await POST({ type: 'QueryAppealMessage', userToken: rootToken, idCard });
				assert.equal(result3.status, 0);
				assert.equal(result3.message, null);
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
