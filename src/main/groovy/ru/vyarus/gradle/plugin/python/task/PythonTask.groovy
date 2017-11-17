package ru.vyarus.gradle.plugin.python.task

import org.gradle.api.GradleException
import org.gradle.api.internal.ConventionTask
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import ru.vyarus.gradle.plugin.python.cmd.Python

/**
 * @author Vyacheslav Rusakov
 * @since 11.11.2017
 */
class PythonTask extends ConventionTask {

    /**
     * Path to directory with python executable. Not required if python installed globally.
     * Automatically set from {@link ru.vyarus.gradle.plugin.python.PythonExtension#pythonPath}, but could
     * be overridden manually.
     */
    @Input
    @Optional
    String pythonPath
    /**
     * Working directory. Not required, but could be useful for some modules (e.g. generators).
     */
    @Input
    @Optional
    String workDir
    /**
     * Module name. If specified, "-m module " will be prepended to specified command (if command not specified then
     * modules will be called directly).
     */
    @Input
    @Optional
    String module
    /**
     * Python command to execute. If module name set then it will be module specific command.
     */
    @Input
    @Optional
    String command
    /**
     * Python logs output level. By default its INFO (visible with -i gradle flag).
     */
    @Input
    @Optional
    LogLevel logLevel
    /**
     * Extra arguments to append to every called command.
     * Useful for pre-configured options, applied to all executed commands
     */
    @Input
    @Optional
    List<String> extraArgs = []
    /**
     * Prefix each line of python output.
     */
    @Input
    @Optional
    String outputPrefix = '\t'

    /**
     * Create work directory if it doesn't exist.
     */
    @Input
    @Optional
    boolean createWorkDir = true

    PythonTask() {
        group = 'python'
    }

    @TaskAction
    void run() {
        String mod = getModule()
        String cmd = getCommand()
        if (!mod && !cmd) {
            throw new GradleException("Module or command to execute must be defined")
        }
        createWorkDirIfRequired()

        Python python = new Python(project, getPythonPath())
                .logLevel(getLogLevel())
                .outputPrefix(getOutputPrefix())
                .workDir(getWorkDir())
                .extraArgs(getExtraArgs())

        if (mod) {
            python.callModule(mod, cmd)
        } else {
            python.exec(cmd)
        }
    }

    /**
     * Add extra arguments, applied to command.
     *
     * @param args arguments
     */
    void extraArgs(List<String> args) {
        if (args) {
            getExtraArgs().addAll(args)
        }
    }

    private void createWorkDirIfRequired() {
        String dir = getWorkDir()
        if (dir && isCreateWorkDir()) {
            File docs = project.file(dir)
            if (!docs.exists()) {
                docs.mkdirs()
            }
        }
    }
}
