package me.gergo

import java.io.File

fun main() {
    val commandLines = File("src/main/resources/input07.txt").readLines()
        .map(::parseCommandLine)

    val root = FileNode("/", 0, null, mutableListOf())
    var currentDir = root
    for (commandLine in commandLines) {
        when (commandLine) {
            is Ls -> continue
            is Cd -> {
                currentDir = if (commandLine.dirName == "..") currentDir.parent!!
                else if (commandLine.dirName == "/") root
                else currentDir.children.find { it.name == commandLine.dirName }!!
            }

            is DirectoryOutput -> currentDir.children.add(FileNode(commandLine.name, 0, currentDir, mutableListOf()))
            is FileOutput -> currentDir.children.add(FileNode(commandLine.name, commandLine.size, currentDir, mutableListOf()))
        }
    }

    // Part One
    val result = root.walk()
        .filter(FileNode::isDirectory)
        .filter { it.totalSize() <= 100000 }
        .sumOf(FileNode::totalSize)
    println("Sum sizes of dirs < 100000: $result")

    // Part Two
    val free = 70000000 - root.totalSize()
    val target = 30000000 - free
    println("Target to free up: $target")
    val toDelete = root.walk()
        .filter(FileNode::isDirectory)
        .filter { it.totalSize() >= target }
        .minBy { it.totalSize() }
    println("To delete: $toDelete, totalSize=${toDelete.totalSize()}")
}

private fun parseCommandLine(line: String): CommandLine {
    return if (line == "$ ls") Ls
    else if (line.startsWith("$ cd")) Cd(line.substring(5))
    else if (line.startsWith("dir ")) DirectoryOutput(line.substring(4))
    else {
        val tokens = line.split(" ")
        FileOutput(tokens[1], tokens[0].toInt())
    }
}

private sealed interface CommandLine
private object Ls : CommandLine {}
private data class Cd(val dirName: String) : CommandLine {}
private data class DirectoryOutput(val name: String) : CommandLine {}
private data class FileOutput(val name: String, val size: Int) : CommandLine {}

private data class FileNode(val name: String, val size: Int, var parent: FileNode?, val children: MutableList<FileNode>) {

    fun isDirectory() = children.size > 0

    fun walk(): Sequence<FileNode> {
        return if (isDirectory()) sequenceOf(this) + children.flatMap(FileNode::walk)
        else sequenceOf(this)
    }

    fun <R> aggregate(transform: (FileNode) -> R, sum: (R, R) -> R): R {
        if (isDirectory()) {
            return (sequenceOf(transform(this)) + children.map { it.aggregate(transform, sum) }).reduce(sum)
        } else {
            return transform(this)
        }
    }

    fun totalSize() = aggregate(FileNode::size, Int::plus)

    override fun toString(): String {
        return if (isDirectory()) "$name (dir)"
        else "$name (file, size=$size)"
    }
}
