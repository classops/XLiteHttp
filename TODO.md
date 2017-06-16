## 待办事项

- [x] 1.请求取消和队列退出
- [x] 2.并发的相关优化
- [x] 3.HurlStack的优化？？？header缺失
- [x] 4.队列的格式问题和优先级比较问题
- [x] 5.Get请求参数
- [ ] 6.Https支持，大致完成，不过需要标准化一下
- [x] 7.Exception的标准？
- [x] 8.headers添加
- [x] 9.请求的回调
WaitingRequests - 缓存请求，在上一个结束回调，添加所有去读缓存。目的只存在一个网络请求，堵塞后面的。完成请求
之后，后面的从缓存中读取。
CurrentRequests - 当前所有，包含所有，方便管理 请求。
- [x] 10.tag 来取消请求
- [x] 11.缓存请求队列
- [x] 12.缓存策略处理，网络请求队列中 - 缓存，缓存队列中，判断是否过期
- [ ] 13.自定义请求，实现Parser便可以
- [ ] 14.验证TrustManager和HostnameVerifier的验证？？？
- [x] 15.缓存OOM后，清除指定缓存等操作

- [x] 16.FINISH方法是否调用
intermediate 和 finish

- [x] 参数数组，请求里Okhttp采用两个List<String>来表达键值对，所以数组也不影响
- [x] Header数组也支持
