# Kahla Notify

A simple Android App that keep listening Kahla channel and notify you instantly.

## ScreenShot

## Introduction

* MainActivity
    1. Start MyService.
    2. If has saved account, then start ConversationListActivity.
    3. If no account saved, then start LoginActivity.
    4. `finish();`.
* LoginActivity
    1. Enter server, email and password.
    2. Login to add a new account to MyService.
    3. Waiting MyService login OK, start ConversationListActivity and then `finish();`.
    4. If any exception thrown, prompt to user.
* ConversationListActivity
    1. Query conversations from MyService immediately whether it's outdated or not.
    2. If cached conversations is outdated, then let MyService get latest conversations. Show refreshing during it refreshing.
    3. Wait conversations fetched, and show.
    4. You can also force refresh by pulling list down.
* ConversationActivity
    1. Query conversation data from MyService.
* LogActivity
    1. Get log from logger.
* MyService
    1. When started, if has saved account, then try to get message list and connect to websocket channel.
    2. Each client has it's own thread loop. One disconnected won't bother others. Everyone retry by themselves.
    3. All data are saved in memory. No database supported.
    4. They has a common logger. Log all error in one file.
    5. MyService provide `add` and `remove` operation of clients.

## Build

```bash
./gradlew assembleRelease
```

**Note: The app released by Travis CI is signed with `debug.keystore`. DON'T TRUST IT!**
