#!/usr/bin/env node

const
	assert = require('assert'),
	exit = () => process.exit(0),
	httpRequest = require('http').request,
	httpsRequest = require('https').request;

const APIConfig = {
	method: 'POST',
	headers: {
		'content-type': 'application/json; charset=utf-8'
	},
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
	// console.log(content.toString());
	return JSON.parse(content);
}

async function startModule(type, test) {
	console.log(`\x1b[33m测试 \x1b[36m${type} \x1b[33m中 \x1b[0m...`);
	await test(type);
	console.log(`\x1b[36m${type} \x1b[32m测试通过！\x1b[0m\n`);
}

async function test() {
	try {
		const idCard = '2021010818';
		let token = '';

		// 注册以及用户已存在
		await startModule('UserRegisterMessage', async type => {
			const data = {
				type,
				userName: 'cat',
				password: 'lsz',
				realName: '猫猫',
				idCard
			};
			let result = await POST(data);
			if (result.status === 0) {
				result = await POST(data);
			}
			assert.equal(result.status, -1);
			assert.equal(result.message, '错误！用户名已经存在了');
		});

		// 登录
		await startModule('UserLoginMessage', async type => {
			const data = {
				type,
				userName: 'cat',
				password: 'lsz'
			};
			const result = await POST(data);
			assert.equal(result.status, 0);
			token = result.message;
			assert.equal(typeof token, 'string');
		});

		console.log(`Token 的值为: ${token}\n`);

		// 政策修改和查询
		const
			place = { province: '卷猫', city: '猫猫', county: '真猫' },
			policies = ['卷宽卷宽喵希喵希！', '猫猫和真猫贴贴！'];

		for (let t = 0; t < 2; ++t) {
			console.log(`\x1b[35m政策修改第 \x1b[32m${t + 1}/2\x1b[35m 轮 ...\x1b[0m\n`);
			await startModule('PolicyUpdateMessage', async type => {
				const data = { type, place, content: policies[t] };
				const result = await POST(data);
				assert.equal(result.status, 0);
				assert.equal(result.message, 1);
			});

			await startModule('PolicyQueryMessage', async type => {
				const data = { type, place };
				const result = await POST(data);
				assert.equal(result.status, 0);
				assert.equal(result.message, policies[t]);
			});
		}

		console.log('\x1b[35m不存在政策测试 ...\x1b[0m\n');
		await startModule('PolicyQueryMessage', async type => {
			const data = { type, place: { province: '不存在的', city: '坏了', county: '没了' } };
			const result = await POST(data);
			assert.equal(result.status, -1);
		});

		console.log('\x1b[35m不合法政策测试 ...\x1b[0m\n');
		await startModule('PolicyQueryMessage', async type => {
			const data = { type, place: { '猫猫': '宽宽' } };
			try {
				await POST(data);
				throw new Error();
			} catch (e) {
				assert.equal(e.message, 'Unexpected token U in JSON at position 0');
			}
		});

		await startModule('UserAddTraceMessage', async type => {
			const data = { type, userToken: '无效 token', idCard, trace: place };
			const result1 = await POST(data);
			assert.equal(result1.status, -1);
			assert.equal(result1.message, '错误！用户不存在或登录信息已过期！');

			data.userToken = token;
			const result2 = await POST(data);
			assert.equal(result2.status, 0);
			assert.equal(result2.message, 1);

			data.trace = { province: '省 2', city: '市 2', county: '区 2' };
			const result3 = await POST(data);
			assert.equal(result3.status, 0);
			assert.equal(result3.message, 1);

			data.trace = { province: '省 3', city: '市 3', county: '区 3' };
			const result4 = await POST(data);
			assert.equal(result4.status, 0);
			assert.equal(result4.message, 1);
		});

		let traces = [];
		await startModule('UserGetTraceMessage', async type => {
			const data = { type, userToken: token, idCard, startTime: 0, endTime: 1e18 };
			const result = await POST(data);
			assert.equal(result.status, 0);
			traces = result.message;
			assert(Array.isArray(traces) && traces.length === 3);
		});

		await startModule('UserUpdateTraceMessage', async type => {
			const data = {
				type, userToken: token, idCard, time: 233,
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

		await startModule('UserDeleteTraceMessage', async type => {
			const data = { type, userToken: token, idCard, time: 233, };
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
			const data = { type, userToken: token, idCard, startTime: 0, endTime: 1e18 };
			const result = await POST(data);
			assert.equal(result.status, 0);
			assert(Array.isArray(result.message) && result.message.length === traces.length);
		});

		console.log('\x1b[35m删除剩余全部数据 ...\x1b[0m\n');
		await startModule('UserDeleteTraceMessage', async type => {
			await Promise.all(
				traces.map(async trace => {
					const data = { type, userToken: token, idCard, time: trace.time };
					const result = await POST(data);
					assert.equal(result.status, 0);
					assert.equal(result.message, 1);
				})
			);
		});

	} catch (e) {
		console.log('测试失败，错误:', e);
	}
}

test();
