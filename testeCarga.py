import asyncio
import aiohttp
import random
import time

URL = "http://localhost:9999/fraud-score"

# Put your full payload list here
PAYLOADS = [
    {
        "expected": "legit",
        "payload": {
            "id":"tx-1",
            "transaction":{"amount":52.4,"installments":2,"requested_at":"2026-03-10T19:12:00Z"},
            "customer":{"avg_amount":120.0,"tx_count_24h":3,"known_merchants":["MERC-001","MERC-002"]},
            "merchant":{"id":"MERC-001","mcc":"5411","avg_amount":88.0},
            "terminal":{"is_online":False,"card_present":True,"km_from_home":22.0},
            "last_transaction":None
        }
    },

    {
        "expected":"fraud",
        "payload":{
            "id":"tx-2",
            "transaction":{"amount":9200.0,"installments":11,"requested_at":"2026-03-14T03:11:00Z"},
            "customer":{"avg_amount":95.0,"tx_count_24h":20,"known_merchants":["MERC-004"]},
            "merchant":{"id":"MERC-999","mcc":"7995","avg_amount":65.0},
            "terminal":{"is_online":False,"card_present":True,"km_from_home":940.0},
            "last_transaction":None
        }
    },

    # add the remaining payloads here...
]

TOTAL_REQUESTS = 1000
DURATION_SECONDS = 60

successful = 0
failed = 0

latencies = []

def percentile(data, p):
    data = sorted(data)
    k = (len(data) - 1) * (p / 100)
    f = int(k)
    c = min(f + 1, len(data) - 1)

    if f == c:
        return data[int(k)]

    d0 = data[f] * (c - k)
    d1 = data[c] * (k - f)

    return d0 + d1

async def send_request(session, index):
    global successful, failed

    payload = random.choice(PAYLOADS)["payload"].copy()

    # avoid duplicated ids
    payload["id"] = f"{payload['id']}-{index}"

    start = time.perf_counter()

    try:
        async with session.post(URL, json=payload) as response:
            response_body = await response.text()

            elapsed_ms = (time.perf_counter() - start) * 1000
            latencies.append(elapsed_ms)

            if response.status == 200:
                successful += 1

                print(
                    f"[{index:04d}] "
                    f"STATUS={response.status} "
                    f"TIME={elapsed_ms:.2f} ms"
                )

            else:
                failed += 1

                print(
                    f"[{index:04d}] "
                    f"STATUS={response.status} "
                    f"TIME={elapsed_ms:.2f} ms "
                    f"BODY={response_body}"
                )

    except Exception as e:
        failed += 1

        elapsed_ms = (time.perf_counter() - start) * 1000

        print(
            f"[{index:04d}] "
            f"ERROR={str(e)} "
            f"TIME={elapsed_ms:.2f} ms"
        )


async def main():
    global latencies

    test_start = time.perf_counter()

    connector = aiohttp.TCPConnector(limit=500)

    timeout = aiohttp.ClientTimeout(total=30)

    async with aiohttp.ClientSession(
        connector=connector,
        timeout=timeout
    ) as session:

        tasks = []

        interval = DURATION_SECONDS / TOTAL_REQUESTS

        for i in range(TOTAL_REQUESTS):
            tasks.append(
                asyncio.create_task(
                    send_request(session, i)
                )
            )

            await asyncio.sleep(interval)

        await asyncio.gather(*tasks)

    total_time = time.perf_counter() - test_start

    print("\n========== RESULTS ==========")

    print(f"Successful requests : {successful}")
    print(f"Failed requests     : {failed}")
    print(f"Total requests      : {successful + failed}")
    print(f"Total duration      : {total_time:.2f} sec")

    if latencies:
        avg = sum(latencies) / len(latencies)

        p50 = percentile(latencies, 50)
        p90 = percentile(latencies, 90)
        p95 = percentile(latencies, 95)
        p99 = percentile(latencies, 99)

        print(f"Average latency     : {avg:.2f} ms")
        print(f"Min latency         : {min(latencies):.2f} ms")
        print(f"Max latency         : {max(latencies):.2f} ms")

        print("\n========== PERCENTILES ==========")
        print(f"P50                 : {p50:.2f} ms")
        print(f"P90                 : {p90:.2f} ms")
        print(f"P95                 : {p95:.2f} ms")
        print(f"P99                 : {p99:.2f} ms")


if __name__ == "__main__":
    asyncio.run(main())