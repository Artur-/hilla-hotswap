package org.vaadin.example.plugin;

import dev.hilla.EndpointController;
import org.hotswap.agent.annotation.*;
import org.hotswap.agent.command.ReflectionCommand;
import org.hotswap.agent.command.Scheduler;
import org.hotswap.agent.javassist.CannotCompileException;
import org.hotswap.agent.javassist.CtClass;
import org.hotswap.agent.javassist.NotFoundException;
import org.hotswap.agent.logging.AgentLogger;
import org.hotswap.agent.util.PluginManagerInvoker;

import static org.hotswap.agent.annotation.LoadEvent.DEFINE;

@Plugin(name = "HillaPlugin", description = "Hotswap agent plugin for Hilla.",
        testedVersions = "2.2",
        expectedVersions = "2.2")
public class HillaPlugin {
    private static AgentLogger LOGGER = AgentLogger.getLogger(HillaPlugin.class);

    @OnClassLoadEvent(classNameRegexp = "dev.hilla.EndpointController")
    public static void registerPlugin(CtClass ctClass) throws NotFoundException, CannotCompileException {
        String src = PluginManagerInvoker.buildInitializePlugin(HillaPlugin.class);
        src += PluginManagerInvoker.buildCallPluginMethod(HillaPlugin.class, "init", "this", "java.lang.Object");
        ctClass.getConstructors()[0].insertAfter(src);
    }

    @Init
    ClassLoader appClassLoader;


    @Init
    Scheduler scheduler;

    public void init(Object endpointController) {
        LOGGER.info("Plugin {} initialized using endpoint controller {}", getClass(), endpointController);
    }

    @OnClassLoadEvent(classNameRegexp = ".*")
    public void classChanged() {
        scheduler.scheduleCommand(
                new ReflectionCommand(this, "dev.hilla.EndpointCodeGenerator", "generate"));
    }

}
