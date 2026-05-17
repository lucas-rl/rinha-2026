import json

input_file = "references.json"
output_file = "dataset.txt"

with open(input_file, "r") as f:
    data = json.load(f)

with open(output_file, "w") as out:

    for item in data:

        vector = ",".join(
            str(v)
            for v in item["vector"]
        )

        label = (
            1
            if item["label"] == "fraud"
            else 0
        )

        out.write(
            f"{vector}|{label}\n"
        )

print("done")