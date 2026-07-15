# AI Builder 网页应用生成平台

## 1\. 项目简介

&emsp;AI Builder网页应用生成平台（ai\-web\-builder）是一款基于 Spring AI、LangChain4j 与 React 技术开发的多智能体项目。平台通过多智能体协作与多轮对话交互，自动完成需求解析、UI 设计、前端代码生成和可视化迭代优化，项目支持实时预览、组件定点修改、源码下载与一键部署，用户无需手写代码，就能快速生成美观的前端网页应用。

## 2\. 运行环境准备
|      软件     |    版本要求   |   用途说明    | 
| :-------------: | :-------------: | :-------------: | 
| JDK  | 21  | 后端项目运行依赖  |
| IntelliJ IDEA | 2023.2.2  | 开发后端项目代码  |
| WebStorm  | 2021.1  | 开发前端项目代码  |
| MySQL | 5.7   | 业务数据持久化存储  |
| Redis  | 3.2.100  | 缓存会话信息、用户登录态及临时验证码  |
| Maven | 3.6+  |项目依赖管理、项目构建（IDEA自带）  |
| nginx  | 1.30.3  | 部署网站  |

## 3\.系统展示

### （1）系统主页

![](https://ai-builder-1384738397.cos.ap-guangzhou.myqcloud.com/show/%E7%BD%91%E7%AB%99%E6%88%AA%E5%9B%BE/6.%E5%AF%B9%E8%AF%9D%E7%A4%BA%E4%BE%8B1.png)

### （2）对话示例

- #### 示例1

![](https://ai-builder-1384738397.cos.ap-guangzhou.myqcloud.com/show/%E7%BD%91%E7%AB%99%E6%88%AA%E5%9B%BE/6.%E5%AF%B9%E8%AF%9D%E7%A4%BA%E4%BE%8B1.png)

![](https://ai-builder-1384738397.cos.ap-guangzhou.myqcloud.com/show/%E7%BD%91%E7%AB%99%E6%88%AA%E5%9B%BE/7.%E7%A4%BA%E4%BE%8B1%E6%95%88%E6%9E%9C%E5%9B%BE.png)

- #### 示例2

![](https://ai-builder-1384738397.cos.ap-guangzhou.myqcloud.com/show/%E7%BD%91%E7%AB%99%E6%88%AA%E5%9B%BE/8.%E5%AF%B9%E8%AF%9D%E7%A4%BA%E4%BE%8B2.png)

![](https://ai-builder-1384738397.cos.ap-guangzhou.myqcloud.com/show/%E7%BD%91%E7%AB%99%E6%88%AA%E5%9B%BE/9.%E7%A4%BA%E4%BE%8B2%E6%95%88%E6%9E%9C%E5%9B%BE.png)

### （3）可视化修改

![](https://ai-builder-1384738397.cos.ap-guangzhou.myqcloud.com/show/%E7%BD%91%E7%AB%99%E6%88%AA%E5%9B%BE/10.%E5%8F%AF%E8%A7%86%E5%8C%96%E4%BF%AE%E6%94%B9.png)

![](https://ai-builder-1384738397.cos.ap-guangzhou.myqcloud.com/show/%E7%BD%91%E7%AB%99%E6%88%AA%E5%9B%BE/11.%E5%8F%AF%E8%A7%86%E5%8C%96%E4%BF%AE%E6%94%B9%E7%BB%93%E6%9E%9C.png)

### （4）注册页面

![](https://ai-builder-1384738397.cos.ap-guangzhou.myqcloud.com/show/%E7%BD%91%E7%AB%99%E6%88%AA%E5%9B%BE/1.%E6%B3%A8%E5%86%8C.png)

### （5）登录页面

![](https://ai-builder-1384738397.cos.ap-guangzhou.myqcloud.com/show/%E7%BD%91%E7%AB%99%E6%88%AA%E5%9B%BE/2.%E5%AF%86%E7%A0%81%E7%99%BB%E5%BD%95.png )

![](https://ai-builder-1384738397.cos.ap-guangzhou.myqcloud.com/show/%E7%BD%91%E7%AB%99%E6%88%AA%E5%9B%BE/3.%E9%AA%8C%E8%AF%81%E7%A0%81%E7%99%BB%E5%BD%95.png)

### （6）用户管理

![](https://ai-builder-1384738397.cos.ap-guangzhou.myqcloud.com/show/%E7%BD%91%E7%AB%99%E6%88%AA%E5%9B%BE/12.%E7%94%A8%E6%88%B7%E7%AE%A1%E7%90%86.png)

### （7）应用管理

![](https://ai-builder-1384738397.cos.ap-guangzhou.myqcloud.com/show/%E7%BD%91%E7%AB%99%E6%88%AA%E5%9B%BE/13.%E5%BA%94%E7%94%A8%E7%AE%A1%E7%90%86.png)

### （8）对话管理

![](https://ai-builder-1384738397.cos.ap-guangzhou.myqcloud.com/show/%E7%BD%91%E7%AB%99%E6%88%AA%E5%9B%BE/14.%E5%AF%B9%E8%AF%9D%E7%AE%A1%E7%90%86.png)

## 4\.技术栈

### 前端技术

- **React \+ TypeScript**：主流前端技术栈，类型约束严谨，代码高可维护

- **Ant Desig  Vue**：企业级 UI 组件库，搭建简洁高效的系统管理界面

- **Iframe 跨域通信技术**：实现预览页面元素拾取、精准定位、可视化编辑核心能力

- **SSE 流式推送**：实现 AI 代码实时流式输出，提升对话交互体验

### 后端技术

- **Spring Boot**：快速构建稳定、高效的后端服务主体架构

- **Spring AI**：对接大模型能力，统一 AI 调用规范，支撑代码生成与对话交互

- **LangChain4j**：实现大模型链式调用、记忆管理、提示词优化、多轮对话上下文维护

- **多智能体协作架构**：拆分为需求设计、图片规划与收集、智能路由、代码质量检查等智能体，分工协同完成建站任务

- **工作流编排**：按工作流节点调用服务和智能体完成搭建网站任务(图片收集、提示词增强、代码生成、代码质量检查、构建项目)

- **Redis**：实现用户登录态持久化、验证码临时存储与对话历史缓存

- **Caffeine**：本地缓存对话历史，提升系统响应速度


## 5\. 核心功能亮点

### （1）AI 智能生成网页

- 基于需求描述，AI 自动生成HTML/多文件/Vue项目代码

- 适配企业官网、个人博客、作品集、购物网站等多场景建站

- 流式输出生成结果，实时预览生成进度，体验流畅

### （2）可视化点选编辑（核心特色）

- 网页元素可视化选中能力，开启编辑模式后，点击页面任意元素即可精准定位


- 基于精准元素上下文，让 AI 定向修改指定模块

### （3）实时预览与即时更新

- 内置 iframe 实时预览窗口，生成/修改后自动刷新页面

- 支持新窗口打开预览页面，沉浸式查看建站效果

### （4）一键部署 \& 代码下载

- 支持下载完整网站源码，可本地二次开发、部署至任意服务器

- 支持平台一键云端部署，快速生成线上访问链接

- 权限隔离：仅创建者可编辑、部署、下载，管理员可查看管理

### （5）对话历史管理

- 自动保存建站对话历史，支持加载更多历史记录

- 追溯每一次生成与修改记录，方便迭代优化页面


## 6\. 使用流程

1. **创建项目**：输入网站需求描述

2. **AI 生成**：点击发送，AI 自动生成完整网页结构与样式

3. **可视化修改**：开启编辑模式，点选页面任意元素，精准修改文字、颜色、布局、样式

4. **预览调试**：实时查看修改效果，持续迭代优化页面

5. **下载/部署**：完成设计后，一键下载源码或部署上线

## 7\. 开源说明

​	本项目为 AI 多智能体开源项目，可供学习、二次开发、个人及商业非商用使用。如需商用部署，请自行完善服务与权限体系。

## 8.后续迭代优化

- 完善工作流整合业务的代码，修改处理流式响应消息的部分
- 解决并发调用瓶颈
- 缓存高频访问、低频更新的数据
- 优化流式响应的实时输出效果
