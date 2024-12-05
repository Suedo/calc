import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {};

if (__ENV.LOAD_TYPE === 'LOW') {
    options = {
        stages: [
            { duration: '10s', target: 10 }, // Ramp-up
            { duration: '30s', target: 10 }, // Steady state
            { duration: '10s', target: 0 },  // Ramp-down
        ],
    };
} else if (__ENV.LOAD_TYPE === 'HIGH') {
    options = {
        stages: [
            { duration: '10s', target: 100 }, // Ramp-up
            { duration: '60s', target: 10000 }, // Steady state
            { duration: '10s', target: 0 },   // Ramp-down
        ],
    };
}

export default function () {
    let res = http.get('http://localhost:8180/test'); // Target API
    check(res, {
        'status is 200': (r) => r.status === 200,
    });
    sleep(0.1); // Simulate user wait time
}


//docker run --rm -e LOAD_TYPE=LOW --network="host" -v $(pwd)/load-test.js:/load-test.js grafana/k6 run /load-test.js | tee output_LOW.log

//docker run --rm -e LOAD_TYPE=HIGH --network="host" -v $(pwd)/load-test.js:/load-test.js grafana/k6 run /load-test.js | tee output_HIGH.log
