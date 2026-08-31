>[!TIP]
>
>This branch just tracks peak stars of aShellYou repository.

#### Reason
> There is a workflow which sends `Github` updates such as `starred-event`, etc. to `Telegram` channel.
> Therefore, to prevent unwanted trigger of workflow `job` by spam `star-unstar` sequence by bots, we record the peak star count and only trigger workflow job only when the peak star count is crossed.
