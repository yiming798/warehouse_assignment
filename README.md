# 仓库管理系统 (Warehouse Management System)

一个基于 JavaFX 的现代化仓库管理系统，用于管理各种类型的物品（消耗品、工具、武器等）。

## 🌟 项目特性

- **多类型物品管理**：支持消耗品（食品、饮料）、工具（工具箱）和武器（枪支、炸弹）
- **GUI 界面**：使用 JavaFX 构建的现代化图形用户界面
- **异常处理**：完善的异常处理机制（过期物品、损坏物品、子弹不足等）
- **数据持久化**：支持文件存储和读取
- **智能排序**：物品自动按类型和属性排序
- **库存清理**：自动清理过期物品功能

## 📁 项目结构

```
src/
├── main/
│   ├── java/com/warehouse/
│   │   ├── exception/      # 异常类
│   │   ├── gui/            # GUI 相关代码（控制器、服务、视图模型）
│   │   ├── manager/        # 仓库管理器
│   │   ├── models/         # 数据模型（物品基类及各种具体物品）
│   │   ├── service/        # 业务服务
│   │   ├── ui/             # 控制台界面
│   │   ├── util/           # 工具类
│   │   ├── WarehouseDemo.java     # 演示入口
│   │   └── WarehouseGuiLauncher.java # GUI 启动入口
│   └── resources/          # 资源文件（FXML, CSS）
└── test/                   # 单元测试
```

## ⚙️ 技术栈

- **编程语言**: Java 25
- **GUI 框架**: JavaFX 25.0.1
- **构建工具**: Maven
- **测试框架**: JUnit 5.9.3

## ▶️ 快速开始

### 运行 GUI 版本
```bash
mvn clean javafx:run -DmainClass=com.warehouse.gui.WarehouseApp
```

### 运行控制台版本
```bash
mvn clean compile exec:java -Dexec.mainClass="com.warehouse.WarehouseDemo"
```

## 🧪 测试

运行所有单元测试：
```bash
mvn test
```

## 📝 项目功能

- **物品管理**: 添加、删除、查询各种类型物品
- **库存监控**: 实时显示库存状态和警告
- **过期管理**: 自动检测和清理过期物品
- **分类统计**: 按类型统计物品数量
- **用户交互**: 友好的图形界面和控制台界面

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📜 许可证

MIT License
