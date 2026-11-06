---
name: fix-issue
description: Fix a GitHub issue
disable-model-invocation: true
---

Fix GitHub issue $ARGUMENTS following our coding standards.

If no issue number is provided in $ARGUMENTS, derive it from the current git branch name (e.g., branch `uchicom/2630` → issue #2630).

1. Determine the issue number: use $ARGUMENTS if provided, otherwise run `git branch --show-current` and extract the trailing number
2. Read the issue description with `gh issue view <number>`
3. Understand the requirements from the issue title and body
4. Implement the fix following the project's coding standards
5. Write tests if applicable
