# 图书管理系统 · 前端

Vue 3 + Vite + Element Plus + Pinia + Axios。对应后端是仓库 src 目录下的 Spring Boot 项目，前端代码在 `frontend` 目录。

## 环境

Node.js 18 以上。

## 启动

```bash
cd frontend
npm install
npm run dev
```

打开 http://localhost:5173

## 测试账号

| 角色   | 账号     | 密码   |
| ------ | -------- | ------ |
| 管理员 | admin    | 123456 |
| 读者   | reader01 | 123456 |

管理员能看到全部菜单，读者登录后只能看首页和自己的借阅记录。

## 关于假数据

~~后端接口还没写，`mock/index.js` 提供了一套内存假数据，dev 模式下由 vite 插件拦截 `/api` 请求。数据在重启 dev 服务后会重置。~~

~~后端写好之后：删掉 `vite.config.js` 里的 `mockApi()`、打开注释掉的 proxy、接口按约定实现。~~

假数据 mock 已删除，后端已实现（Spring Boot + MyBatis-Plus，见仓库 `src` 目录）。dev 模式下 `/api` 请求由 vite 代理到 `http://localhost:8080`，先启动后端再启动前端即可联调。

## 接口约定

返回格式统一为 `{ code, msg, data }`，`code = 200` 表示成功，失败时 `msg` 为错误提示。无返回数据的操作接口 `data` 随便给个非空值（比如 `true`）。

| 方法   | 路径                     | 说明                                                         |
| ------ | ------------------------ | ------------------------------------------------------------ |
| POST   | /api/login               | 登录，body: `{ username, password }`，返回 token、role、name、readerId |
| GET    | /api/stats               | 首页统计：bookTotal / readerTotal / borrowing / overdue      |
| GET    | /api/category/list       | 分类列表                                                     |
| POST   | /api/category            | 新增分类                                                     |
| DELETE | /api/category/{id}       | 删除分类                                                     |
| GET    | /api/book/page           | 图书分页，参数 pageNum / pageSize / keyword / categoryId     |
| GET    | /api/book/all            | 全部图书（下拉框用）                                         |
| POST   | /api/book                | 新增图书                                                     |
| PUT    | /api/book                | 修改图书（body 带 bookId）                                   |
| DELETE | /api/book/{id}           | 删除图书                                                     |
| GET    | /api/reader/page         | 读者分页，参数 pageNum / pageSize / keyword                  |
| GET    | /api/reader/all          | 全部读者（下拉框用，带当前借阅数 borrowing）                 |
| POST   | /api/reader              | 新增读者                                                     |
| PUT    | /api/reader              | 修改读者（body 带 readerId）                                 |
| DELETE | /api/reader/{id}         | 删除读者                                                     |
| GET    | /api/borrow/page         | 借阅分页，参数 pageNum / pageSize / status / keyword / readerId，行数据带 bookTitle、readerName、cardNo |
| POST   | /api/borrow              | 办理借书，body: `{ bookId, readerId, days }`                 |
| PUT    | /api/borrow/return/{id}  | 归还                                                         |
| PUT    | /api/borrow/renew/{id}   | 续借 30 天                                                   |
| GET    | /api/notice/page         | 公告分页，参数 pageNum / pageSize                            |
| POST   | /api/notice              | 发布公告                                                     |
| PUT    | /api/notice              | 修改公告（body 带 noticeId）                                 |
| DELETE | /api/notice/{id}         | 删除公告                                                     |

## 其他

- 新增读者不会自动创建登录账号（user 表），账号开通需要后端单独处理。
- 后端业务规则：违约次数达到 3 次暂停借书、借满上限不能再借、有未归还图书的图书/读者不能删除、逾期归还记一次违约。
- 打包产物在 `frontend/dist`，可以直接放到 Spring Boot 的 `resources/static` 下托管，打包后走真实接口。目前 `src/main/resources/static` 下已放置一份，重新打包前端（`npm run build`）后需重新复制过去。
