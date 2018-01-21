package com.fabien.kblock.interfaces.cli

abstract class CommandLine(commands: List<Command>) {
    abstract val name : String
    abstract val description : String

    private val commands = commands

    fun help() {
        println("$name: $description")
        println()
        println("Commands:")
        for (command in commands) {
            println("${command.name}\t\t${command.description}")
        }
    }

    fun execute(args: List<String>) {
        if (args.isEmpty()) {
            help()
        }
        val commandStr = args[0]
        if (commands.none { it.name == commandStr }) {
            throw IllegalArgumentException("$name: $commandStr is not a valid command")
        }
        commands.first { it.name == commandStr }.prepareExecution(args.drop(1))
    }
}

abstract class Command {
    abstract val name: String
    abstract val description: String

    abstract val options: List<Option>

    private fun fillOptionsValue(args: List<String>) {
        val iterator = args.iterator()
        while (iterator.hasNext()) {

            val option = parseOptionName(iterator.next())

            if (!iterator.hasNext()) {
                throw IllegalStateException("Missing value for option {$option}")
            }
            option.value = iterator.next()
        }
    }

    private fun parseOptionName(optionNameArg: String): Option {
        return when {
            optionNameArg.startsWith("--") -> {
                val optionNameStr = optionNameArg.substring(2)
                if (!options.none { it.name == optionNameStr }) {
                    throw IllegalArgumentException("$optionNameStr does not exist")
                }
                options.first { it.name == optionNameStr }
            }
            optionNameArg.startsWith("-") -> {
                val optionNameStr = optionNameArg.substring(1)
                if (options.none { it.shortName == optionNameStr }) {
                    throw IllegalArgumentException("$optionNameStr does not exist")
                }
                options.first { it.shortName == optionNameStr }
            }
            else -> throw IllegalArgumentException("$optionNameArg is not formatted as an option (--name | -shortName)")
        }
    }

    fun prepareExecution(args: List<String>) {
        fillOptionsValue(args)
        when {
            options.all { it.value == "" } -> HelpCommand.execute()
            options.any { it.value == "" } -> throw IllegalArgumentException("Missing option value for command $name")
            else -> execute()
        }
    }

//    fun help() {
//        println("$name ${HelpCommand.name}: ${HelpCommand.description}")
//        println()
//        println("Options:")
//        for (option in HelpCommand.options) {
//            println("-${option.shortName},--${option.name}\t\t\t${option.description}")
//        }
//    }

    abstract fun execute()
}

interface Option {
    val name : String
    val shortName: String
    val description : String
    var value : String
}

object HelpCommand : Command() {
    override val name = "help"
    override val description = "display help on all commands available"
    override val options = listOf<Option>()

    override fun execute() {
    }
}