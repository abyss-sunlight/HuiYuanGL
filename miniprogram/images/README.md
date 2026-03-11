# 图标说明

此目录用于存放底部导航栏的图标文件。

## 需要的图标文件

- `home.png` - 主页图标（未选中状态）
- `home-active.png` - 主页图标（选中状态）
- `records.png` - 记录图标（未选中状态）
- `records-active.png` - 记录图标（选中状态）
- `profile.png` - 我的图标（未选中状态）
- `profile-active.png` - 我的图标（选中状态）

## 图标规格

- **尺寸**：建议 81px × 81px
- **格式**：PNG
- **颜色**：
  - 未选中：灰色 (#999999)
  - 选中：蓝色 (#007aff)

## 临时解决方案

如果暂时没有图标文件，可以：

1. 从微信小程序官方示例中复制图标
2. 使用在线图标库（如 iconfont）下载
3. 暂时移除 tabBar 中的 iconPath 配置（只显示文字）

## 移除图标配置（临时）

如果暂时没有图标，可以修改 `app.json` 中的 tabBar 配置：

```json
{
  "tabBar": {
    "color": "#999999",
    "selectedColor": "#007aff",
    "backgroundColor": "#ffffff",
    "borderStyle": "black",
    "list": [
      {
        "pagePath": "pages/index/index",
        "text": "主页"
      },
      {
        "pagePath": "pages/records/records",
        "text": "记录"
      },
      {
        "pagePath": "pages/profile/profile",
        "text": "我的"
      }
    ]
  }
}
```
