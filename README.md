# Legado Lite · 安卓开源阅读 APP

一个仿「开源阅读（Legado）」风格的 Android Kotlin 原生阅读 APP，集成本地 TTS 听书、联网书源系统、Compose UI。

## 项目特点

- **联网书源**：JSON 配置书源，跨书源搜索、解析、聚合
- **轻量 TTS 听书**：Android 系统 TTS + Sherpa-onnx 引擎骨架（流式播放）
- **类 Legado UI**：书架 / 发现 / 搜索 / 详情 / 阅读器 / 设置 全套
- **可配置阅读偏好**：字号 / 行距 / 背景色（白/护眼绿/黑）/ 翻页模式（滑动/仿真）
- **Kotlin + Jetpack Compose + Room + OkHttp + ExoPlayer**

## 编译方法（5 分钟出 APK）

### 准备
1. 安装 [Android Studio](https://developer.android.com/studio)（推荐 Hedgehog 或更新版本）
2. 安装 JDK 17（Android Studio 自带）
3. 准备一台 Android 7.0+ 手机

### 步骤
1. 解压 `安卓阅读APP.tar.gz`：
   ```bash
   tar -xzf 安卓阅读APP.tar.gz
   cd 安卓阅读APP
   ```

2. 用 Android Studio 打开 `安卓阅读APP` 文件夹（File → Open → 选中 `build.gradle.kts`）

3. 等待 Gradle Sync 完成（首次会下载约 200MB 依赖，国内网络可能要 10-30 分钟；可配置阿里云镜像加速）

4. 编译 APK：
   - 顶部菜单：**Build → Build Bundle(s) / APK(s) → Build APK(s)**
   - 或快捷键：`Ctrl+Shift+A`（Win/Linux）/ `Cmd+Shift+A`（Mac）→ 输入 "Build APK"

5. 编译完成后，APK 在 `app/build/outputs/apk/debug/app-debug.apk`

6. 把 APK 传到手机安装（需要开启"未知来源应用"）

## 命令行编译（可选）

如果不想用 Android Studio GUI：
```bash
cd 安卓阅读APP
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## 首次使用

1. 安装后打开 APP
2. 进入 **设置 → 书源管理** → 导入 `app/src/main/assets/book_sources/examples.json`（示例书源，规则可改）
3. 或者添加自己的书源（参考下方书源规则说明）
4. 回到 **搜索**，输入书名查找
5. 找到后 → 加入书架 → 阅读
6. 阅读时点右上角 ▶️ 开始听书

## 书源规则（Legado 兼容）

书源是 JSON 格式，核心字段：

| 字段 | 含义 | 示例 |
|------|------|------|
| `bookSourceName` | 书源名称 | `"示例书源A"` |
| `bookSourceUrl` | 书源根域名 | `"https://example.com"` |
| `searchUrl` | 搜索 URL 模板 | `"https://example.com/search?q={{key}}"` |
| `ruleSearchList` | 搜索结果列表 CSS/XPath/JSONPath | `"ul.result-list li"` |
| `ruleSearchName` | 书名规则 | `"h3@text"` |
| `ruleSearchAuthor` | 作者规则 | `"p.author@text"` |
| `ruleSearchUrl` | 详情页 URL 规则 | `"a@href"` |
| `ruleTocList` | 目录列表规则 | `"ul.chapter-list li"` |
| `ruleTocName` | 章节名规则 | `"a@text"` |
| `ruleTocUrl` | 章节 URL 规则 | `"a@href"` |
| `ruleContent` | 正文规则 | `"div#content@text"` |

支持的后缀规则：
- `@text` 提取文本
- `@html` 提取 HTML
- `@src` / `@href` 提取属性
- `##` 在文本中过滤标签
- `{{key}}` 搜索关键词占位

JSON 解析支持 `$.field` 语法（JSONPath 子集）。

## TTS 引擎说明

默认使用 Android 系统 TTS（`TextToSpeech`），开箱即用。

如需启用更高质量的 **Sherpa-onnx** 离线 TTS：
1. 下载预训练模型（推荐 `vits-zh-aishell3`）：https://github.com/k2-fsa/sherpa-onnx/releases
2. 把模型目录复制到 `Android/data/com.legado.lite/files/tts/vits-zh-aishell3/`
3. 引入 sherpa-onnx-android JNI（参见 sherpa-onnx 文档）
4. 重新编译

## 模块结构

```
app/src/main/java/com/legado/lite/
├── data/        # 数据层：Room DB、DAO、Repository、HTTP、书源解析
├── domain/      # 领域层：模型、Repository 接口
├── tts/         # TTS 引擎：系统 TTS + Sherpa-onnx
├── ui/          # UI 层：Compose 屏幕 + ViewModel
└── MainActivity.kt
```

## 技术栈

- Kotlin 1.9 + Coroutines + Flow
- Jetpack Compose（Material 3）
- Room 2.6
- OkHttp 4.12
- ExoPlayer（Media3）
- KSP（编译时注解处理）
- Kotlinx Serialization

## 已知限制

- 本工程未做完整单元测试
- Sherpa-onnx JNI 集成需用户自行补全（README 里有说明）
- 仅支持 Android 7.0+（API 24+）
- 部分高阶 Legado 书源规则（如 put/post、分页搜索）暂未实现

## 许可

个人项目，代码仅供参考。
