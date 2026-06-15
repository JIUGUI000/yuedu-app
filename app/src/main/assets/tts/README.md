# 听书 TTS 模型放置说明

本工程默认使用 Android 系统 TTS（`TextToSpeech`）保证开箱即用。

如果你希望启用 sherpa-onnx 本地离线 TTS（更高质量、可朗读中文数字段），
请按下列步骤把模型放进 `app/src/main/assets/tts/` 或运行时下载到
`Android/data/com.legado.lite/files/tts/`：

1. 下载预训练模型（推荐）：
   - vits-zh-aishell3
   - 或 vits-melo-tts-zh_en
2. 把模型目录复制到：
   - `Android/data/com.legado.lite/files/tts/vits-zh-aishell3/`
3. 目录至少包含：
   - `model.onnx`
   - `tokens.txt`
4. 引入 sherpa-onnx-android 的 .so / .jar（参见 README 的"接入 sherpa-onnx"章节）
5. 重新编译后，App 启动时会自动检测并切换到 sherpa-onnx 引擎。

> 注：sherpa-onnx 的 JNI 集成因作者发布渠道不同而不同，常见来源：
> https://github.com/k2-fsa/sherpa-onnx/releases
