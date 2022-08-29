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
	return JSON.parse((await request({ ...APIConfig, data: JSON.stringify(data) }))[1]);
}

async function startModule(type, test) {
	console.log(`\x1b[33m测试 \x1b[36m${type} \x1b[33m中 \x1b[0m...`);
	await test(type);
	console.log(`\x1b[36m${type} \x1b[32m测试通过！\x1b[0m\n`);
}

async function test() {
	try {
		let token = '';

		await startModule('UserRegisterMessage', async type => {
			const data = {
				type,
				userName: 'cat',
				password: 'lsz',
				realName: '猫猫',
				idCard: '2021010818'
			};
			let result = await POST(data);
			if (result.status === 0) {
				result = await POST(data);
			}
			assert.equal(result.status, -1);
			assert.equal(result.message, '错误！用户名已经存在了');
		});

		await startModule('UserLoginMessage', async type => {
			const data = {
				type,
				userName: 'cat',
				password: 'lsz'
			};
			const result = await POST(data);
			assert.equal(result.status, 0);
			token = result.message;
			assert(typeof token === 'string');
		});

		const
			place = {
				province: '卷猫',
				city: '猫猫',
				county: '真猫'
			},
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

		await startModule('PolicyQueryMessage', async type => {
			const data = { type, place: { province: '不存在的', city: '坏了', county: '没了' } };
			const result = await POST(data);
			console.log(result);
		});



	} catch (e) {
		console.log('测试失败，错误:', e);
	}
}

test();
