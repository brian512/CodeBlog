CodeBlog是为了方便在手机端阅读编程技术博文。集成展示了CSDN博客、InfoQ、开源中国和ITEye上的技术博文，并提供了技术类型的选择，如移动开发、数据库、云计算等，让你随时随地都可以轻松阅读技术大牛的文章。

目前已实现的功能：
 - 博客查看、收藏与分享；
 - 博文高亮显示代码；
 - CSDN博客搜索；
 - 分享博文或应用到QQ好友；
 - 机器人陪聊；
 - 文章推送；

---
####更新日志：

v1.3.76
- 集成小米推送模块；
- 使用OkHttp进行网络请求；
- 博文文件缓存；
- 收藏与历史记录使用数据库存储；
- 查看博主文章列表；
- 使用Picasso加载图片；
- UI改进；

v1.3.75
- 集成小米更新模块；
- 优化首次打开博客卡顿；
- 增加水波纹效果；

v1.3.74
- 修复CSDN列表解析；
- 修复首次加载X5内核ANR；
- 源代码提交github，地址见关于页面；

v1.3.73
- 优化X5内核加载；
- 使用SwipeRefreshLayout；
- 增加推送功能；

v1.3.72
- 修复博文列表及内容解析出错；
- 集成腾讯X5内核；
- 优化UI；


---
使用到的技术点：
 - [Netroid](https://github.com/vince-styling/Netroid)(Volley拓展库)用于网络请求；
 - [Jsoup](https://github.com/jhy/jsoup)解析网页；
 - 友盟推送、统计、在线参数；
 - 小米更新模板；
 - [EventBus](https://github.com/greenrobot/EventBus)
 - [bugly](https://bugly.qq.com/v2/)异常收集；
 - [Gson](https://github.com/google/gson)进行序列化；
 - [litepal](https://github.com/LitePalFramework/LitePal)数据库存储；
 - [SwipebackLayout](https://github.com/ikew0ng/SwipeBackLayout)滑动退出activity；
 - [TabLayout](http://blog.csdn.net/brian512/article/details/51793430)实现主页tab切换；
 - [SlidingMenu](https://github.com/jfeinstein10/SlidingMenu)实现侧滑栏；

----------------
加群讨论技术和功能，欢迎大家参与！
![这里写图片描述](http://img.blog.csdn.net/20160817112120745)
