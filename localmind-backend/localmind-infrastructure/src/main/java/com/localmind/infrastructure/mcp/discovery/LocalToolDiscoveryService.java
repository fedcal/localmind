package com.localmind.infrastructure.mcp.discovery;

import com.localmind.domain.mcp.port.out.LocalToolDiscoveryPort;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Discovers all local @Tool methods registered in the Spring context.
 * Scans beans whose class name starts with "LocalMind" and ends with "Tools"
 * for methods annotated with @Tool, extracting name and description.
 */
@Component
public class LocalToolDiscoveryService implements LocalToolDiscoveryPort {

    private final ApplicationContext applicationContext;
    private volatile List<Map<String, Object>> cachedTools;

    public LocalToolDiscoveryService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Returns all discovered local @Tool methods as a list of maps
     * with keys: name, description, local.
     * Results are cached after first invocation.
     */
    @Override
    public List<Map<String, Object>> discoverLocalTools() {
        if (cachedTools != null) {
            return cachedTools;
        }

        List<Map<String, Object>> tools = new ArrayList<>();
        String[] beanNames = applicationContext.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            Object bean;
            try {
                bean = applicationContext.getBean(beanName);
            } catch (Exception e) {
                continue;
            }

            Class<?> beanClass = bean.getClass();
            String className = beanClass.getSimpleName();

            if (!className.startsWith("LocalMind") || !className.endsWith("Tools")) {
                continue;
            }

            for (Method method : beanClass.getDeclaredMethods()) {
                Tool toolAnnotation = method.getAnnotation(Tool.class);
                if (toolAnnotation != null) {
                    Map<String, Object> toolInfo = Map.of(
                            "name", method.getName(),
                            "description", toolAnnotation.description(),
                            "local", true
                    );
                    tools.add(toolInfo);
                }
            }
        }

        cachedTools = Collections.unmodifiableList(tools);
        return cachedTools;
    }
}
