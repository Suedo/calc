import http from 'k6/http';
import { check, sleep } from 'k6';

// Load testing options
export const options = {
    stages: [
        { duration: '10s', target: 10 }, // Ramp-up to 50 users
        { duration: '30s', target: 10 }, // Steady state at 100 users
        { duration: '10s', target: 0 },  // Ramp-down
    ],
};

export default function () {
//    let res = http.get('http://172.17.0.1:8180/test'); // Adjust the target if needed
    let res = http.get('http://localhost:8180/test'); // when triggering from terminal
//    let res = http.get('http://host.docker.internal:8180/test'); // when running from docker
    check(res, {
        'status is 200': (r) => r.status === 200,
    });
    sleep(0.1); // Simulate user wait time
}
// docker run --rm -i grafana/k6 run --out prometheus=http://host.docker.internal:9090 - < ./load-test.js


// docker run --rm --network="host" -v $(pwd)/load-test.js:/load-test.js grafana/k6 run /load-test.js > output.log
