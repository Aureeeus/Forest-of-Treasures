---
description: A workflow for safely committing and pushing changes to a remote Git repository
---
# Git Push Workflow

This workflow ensures that changes are committed with clear, descriptive messages and safely pushed to the remote repository without causing conflicts.

1. **Review and organize staged changes:** Using `git status` and `git diff --cached`, review the currently staged changes. Understand what the files are about and what modifications have been made to provide a descriptive commit message. If no changes are staged, add them contextually using `git add <files>`.
2. **Commit the changes:** Create a commit message that is clear, readable, and adheres to standard Git commit guidelines (e.g., using conventional commits like `feat:`, `fix:`, `docs:`, etc., if applicable or at least a clear imperative mood summary).
3. **Compose a detailed commit message summary:** When making your commit, include a bulleted list of what was changed in the commit description to provide clear context for reviewers and future reference. Ensure the summary is highly readable and easy to understand.
   ```bash
   # example
   git commit -m "feat: user authentication system
   
   - Added JWT-based login and registration endpoints
   - Configured secure HTTP-only cookies for token storage
   - Updated User model to include password hashing"
   ```
4. **Fetch remote updates:** After committing, use `git fetch` to check if there are newly pushed updates on the remote repository.
5. **Handle remote updates safely:** Run `git status` to see if your local branch is behind the remote tracking branch.
   - If there *are* updates on the remote and your local branch is tracking it, be cautious. You can stash any active unstaged changes if needed (`git stash`). Notify the user to avoid pushing conflicts, or proceed to pull with rebase/merge if standard practice, ensuring we avoid overwriting or creating messy conflict resolutions without user awareness.
6. **Ensure no conflicts:** Use `git status`, `git log origin/main..HEAD` (or the relevant remote branch), or `git diff` to verify the state between your local commits and the remote before attempting to push. Resolving conflicts locally if a pull was required is mandatory before pushing.
7. **Push to the remote repository:** When all conflicts are resolved or there are no remote updates to worry about, safely push to the remote repository. Inform the user what branch you are pushing to before or after doing so.
   ```bash
   git push origin <branch-name>
   ```
