---
description: Safely commit and push changes to the remote repository
---

# Safe Git Commit & Push

Use this workflow whenever committing and pushing changes to avoid pulling unwanted files (e.g. gradle configs) that can cause project conflicts.

## Steps

// turbo
1. Check the current branch and working tree status:
```
git status
```

2. Stage only the relevant files for the commit:
```
git add <file1> <file2> ...
```

3. Commit with a meaningful, descriptive message:
```
git commit -m "<type>: <short summary>

<detailed bullet points explaining what changed and why>"
```

// turbo
4. Fetch the latest remote changes **without** modifying the working tree:
```
git fetch origin <branch-name>
```

// turbo
5. Check if there are any new remote commits that could conflict:
```
git log HEAD..origin/<branch-name> --oneline
```

6. **If no new commits** — push directly:
```
git push origin <branch-name>
```

7. **If new commits exist** — notify the user about the remote changes and ask how to proceed (rebase, merge, or abort) before pushing.

> **IMPORTANT**: Do NOT use `git pull` or `git pull --rebase` as it pulls all remote content into the working tree, which can conflict with local gradle and build files.
