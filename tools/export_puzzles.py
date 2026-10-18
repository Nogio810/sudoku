"""Build Android's bundled catalog using the existing Python generator and grader.

python tools/export_puzzles.py --generator-root C:/path/to/sudoku-generator
The generator remains a separate project; Android never runs Python.
"""

import argparse
import hashlib
import json
import random
import sys
from pathlib import Path


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--generator-root", type=Path, required=True)
    parser.add_argument("--output", type=Path, default=Path(__file__).resolve().parents[1] / "app/src/main/assets/puzzles.json")
    parser.add_argument("--per-level", type=int, default=2)
    parser.add_argument("--max-candidates", type=int, default=1200)
    parser.add_argument("--seed", type=int, default=20261018)
    args = parser.parse_args()
    if args.per_level < 1 or args.max_candidates < 1:
        parser.error("counts must be positive")
    sys.path.insert(0, str(args.generator_root.resolve() / "src"))
    from sudoku_generator.generator import GenerationError, Generator
    from sudoku_generator.grading.grader import Grader

    catalog = []
    counts = {level: 0 for level in range(1, 7)}
    seen = set()
    rng = random.Random(args.seed)
    grader = Grader()
    # Evaluator v2 requires <=30 holes for level 1 and <=42 for level 2.
    # Fill these separately: more candidates with >=36 holes can never yield level 1.
    harder_holes = (45, 50, 53, 55, 56, 57, 58)
    for attempt in range(args.max_candidates):
        seed = rng.randrange(2**31)
        if counts[1] < args.per_level:
            holes = rng.choice((24, 26, 28))
        elif counts[2] < args.per_level:
            holes = rng.choice((36, 39, 40))
        else:
            holes = rng.choice(harder_holes)
        try:
            puzzle, solution = Generator(seed=seed).generate_puzzle(holes)
            rating = grader.grade(puzzle)
        except GenerationError:
            continue
        if rating.status != "rated":
            continue
        level = int(rating.level)
        fingerprint = "".join(str(value) for row in puzzle for value in row)
        if counts[level] >= args.per_level or fingerprint in seen:
            continue
        seen.add(fingerprint)
        counts[level] += 1
        catalog.append({
            "id": "sudoku-" + hashlib.sha256(fingerprint.encode()).hexdigest()[:16],
            "puzzle": puzzle, "solution": solution, "holes": holes,
            "rating": rating.to_dict(), "source": {"generator_seed": seed},
        })
        print(f"candidate {attempt + 1}: level {level}, counts {counts}", flush=True)
        if all(count == args.per_level for count in counts.values()):
            break
    else:
        # Preserve any previous catalog if the requested set cannot be generated.
        raise SystemExit(f"Not enough rated puzzles: {counts}. Increase --max-candidates.")
    catalog.sort(key=lambda entry: (entry["rating"]["level"], entry["id"]))
    payload = {"schema_version": 1, "puzzles": catalog}
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Saved {len(catalog)} puzzles to {args.output}", flush=True)


if __name__ == "__main__":
    main()
