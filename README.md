总体架构：Clean & SOLID启发的四层架构
本框架采用一个内核精炼、功能分层、依赖清晰的架构，其核心设计思想是 “稳定内核，可拔插增强”。整个架构围绕一个纯粹的核心引擎展开，通过标准的接口与各种“增强器”连接，以实现编译期分析、运行时监控等高级功能，而所有决策权最终都交还给开发者。

graph TD
    subgraph A [核心引擎层 Core Engine]
        A1[DSL & 流程定义]
        A2[类型安全的流程执行器]
        A3[统一的执行模型]
    end

    subgraph B [静态增强层 (可选)]
        B1[注解处理器]
        B2[KSP 静态分析器]
        B3[生成：洞察报告/流图文档]
    end

    subgraph C [运行时增强层 (可选)]
        C1[拦截器接口]
        C2[指标收集插件]
        C3[追踪与诊断插件]
    end

    subgraph D [外围集成层]
        D1[Ktor/Spring Boot 适配器]
        D2[Web 仪表盘]
        D3[IDE 插件]
    end

    B -- “基于” --> A
    C -- “注入” --> A
    D -- “使用或暴露” --> A & B & C
    
    style A fill:#e1f5e1
    style B fill:#e3f2fd
    style C fill:#fff3e0
    style D fill:#fce4ec