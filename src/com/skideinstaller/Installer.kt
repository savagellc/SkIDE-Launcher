package com.skideinstaller

import javafx.application.Application
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.stage.Stage
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class Controller {

    @FXML
    lateinit var label: Label

    @FXML
    lateinit var progessBar: ProgressBar
    @FXML
    lateinit var cancelBtn: Button
}

class Installer : Application() {
    override fun start(primaryStage: Stage) {

        primaryStage.title = "SkIde Launcher"

        val loader = FXMLLoader()
        val parent = loader.load<Parent>(javaClass.getResourceAsStream("Gui.fxml"))
        val controller = loader.getController<Controller>()
        primaryStage.scene = Scene(parent)
        primaryStage.centerOnScreen()
        primaryStage.isResizable = false
        primaryStage.sizeToScene()
        primaryStage.show()


        val task = object : Task<Void>() {
            @Throws(Exception::class)
            override fun call(): Void? {
                updateProgress(0.0, 100.0)
                updateMessage("Checking files...")

                val version = checkVersion()
                println(version)
                val folder = File(System.getProperty("user.home"), ".skide")
                val binFolder = File(folder, "bin")
                val versionFile = File(binFolder, "version.txt")


                if (!folder.exists()) {
                    folder.mkdir()
                    if (!binFolder.exists()) binFolder.mkdir()
                    updateProgress(25.0, 100.0)
                    updateMessage("Updating SkIde...")
                    update()
                    if(!versionFile.exists()) versionFile.createNewFile()
                    Files.write(versionFile.toPath(), version.toByteArray(), StandardOpenOption.TRUNCATE_EXISTING)
                    return null
                }
                if (!binFolder.exists()) {
                    binFolder.mkdir()
                    updateProgress(25.0, 100.0)
                    updateMessage("Updating SkIde...")
                    update()
                    if(!versionFile.exists()) versionFile.createNewFile()

                    Files.write(versionFile.toPath(), version.toByteArray(), StandardOpenOption.TRUNCATE_EXISTING)

                    return null
                }
                val ideFile = File(binFolder, "SkIde.jar")
                if (!ideFile.exists()) {
                    update()
                    updateProgress(25.0, 100.0)
                    updateMessage("Updating SkIde...")
                    if(!versionFile.exists()) versionFile.createNewFile()

                    Files.write(versionFile.toPath(), version.toByteArray(), StandardOpenOption.TRUNCATE_EXISTING)
                    return null
                }
                if (!versionFile.exists()) {
                    update()
                    updateProgress(25.0, 100.0)
                    updateMessage("Updating SkIde...")
                    if(!versionFile.exists()) versionFile.createNewFile()
                    Files.write(versionFile.toPath(), version.toByteArray(), StandardOpenOption.TRUNCATE_EXISTING)
                    return null
                }
                val oldVersion = String(Files.readAllBytes(versionFile.toPath()))

                if(oldVersion != version) {
                    update()
                    updateProgress(25.0, 100.0)
                    updateMessage("Updating SkIde...")
                    Files.write(versionFile.toPath(), version.toByteArray(), StandardOpenOption.TRUNCATE_EXISTING)
                    return null
                } else {
                    updateProgress(100.0, 100.0)
                    updateMessage("Starting SkIde")
                    Thread.sleep(2500)
                    start()
                }


                return null
            }
        }
        controller.label.textProperty().bind(task.messageProperty())
        controller.progessBar.progressProperty().bind(task.progressProperty())

        val thread = Thread(task)
        thread.isDaemon = true

        controller.cancelBtn.setOnAction {
            task.cancel()
            System.exit(0)
        }
        thread.start()
    }


    fun checkVersion(): String {

        var str = ""
        val response = request("https://liz3.net/sk/depot/").third

        while (true) {
            val r = response.read()
            if (r == -1) break
            str += r.toChar()
        }
        return str
    }

    fun start() {

        val folder = File(System.getProperty("user.home"), ".skide")
        val binFolder = File(folder, "bin")
        val ideFile = File(binFolder, "SkIde.jar")

        Thread {
            val java = File(File(System.getProperty("java.home"), "bin"), "java").absolutePath
            println(java)
            val args = arrayListOf<String>(java, "-jar", ideFile.absolutePath)
            val pb = ProcessBuilder()
            args += State.args
            pb.command(args)
            pb.start()
            System.exit(0)
        }.start()
    }

    fun update() {
        val folder = File(System.getProperty("user.home"), ".skide")
        val binFolder = File(folder, "bin")
        val ideFile = File(binFolder, "SkIde.jar")

        downloadFile("https://liz3.net/sk/depot/SkIde.jar", ideFile.absolutePath)
        start()
    }

    companion object {
        fun start() {
            launch(Installer::class.java)
        }
    }
}