package presentation

import controller.TestController
import domain.TestEvents.TableResultsModel
import scalafx.Includes._
import scalafx.application.{JFXApp, Platform}
import scalafx.beans.property.DoubleProperty
import scalafx.geometry.{Insets, Pos, Side}
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control._
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout._
import scalafx.scene.text.Font
import scalafx.scene.{Node, Scene}
import scalafx.stage.{DirectoryChooser, WindowEvent}
import util.AppConfig

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

/**
  * Created by nuno on 28-04-2017.
  */
object MainApplicationGUI extends JFXApp {

  val theController = new TestController


  val table = createResultsTableNode()

  val concurrentLabel = new Label("1");
  val concurrentTask = new Slider(1, 20, 1)

  val concurrentNumberObserver = new DoubleProperty(this, "Concurrent", 0)
  concurrentNumberObserver <== concurrentTask.value

  concurrentNumberObserver.delegate.onChange((_, _, newValue) => {
    concurrentLabel.text = newValue.intValue().toString
  })


  val nodeErrorLabel: Label = new Label()
  val nodeText: TextField = new PersistentPromptTextField("", "google.pt") {
    focused.onChange((obs, oldValue, newValue) => {
      if (oldValue) {
        if (text.value.isEmpty) {

          if (!styleClass.contains("error"))
            styleClass += "error"

          styleClass -= "ok"

          nodeErrorLabel.text = "Node is required"
        } else {
          if (styleClass.contains("error"))
            styleClass -= "error"

          styleClass += "ok"

          nodeErrorLabel.text = ""
        }
      }
    })
  }

  val portErrorLabel: Label = new Label()
  val portText: TextField = new PersistentPromptTextField("", "443") {
    focused.onChange((obs, oldValue, newValue) => {
      if (oldValue) {
        if (text.value.isEmpty) {
          if (!styleClass.contains("error"))
            styleClass += "error"
          styleClass -= "ok"
          portErrorLabel.text = "Port is required"
        } else {
          if (text.value.matches("[0-9]*")) {
            styleClass += "ok"
            portErrorLabel.text = ""
          } else {
            if (!styleClass.contains("error"))
              styleClass += "error"
            portErrorLabel.text = "Port must be a natural number"
            styleClass -= "ok"
          }
        }
      }
    })
  }

  val durationErrorLabel: Label = new Label()
  val durationText: TextField = new PersistentPromptTextField("", "5 minutes") {

    focused.onChange((obs, oldValue, newValue) => {
      if (oldValue) {
        if (text.value.isEmpty) {
          if (!styleClass.contains("error"))
            styleClass += "error"
          styleClass -= "ok"
          durationErrorLabel.text = "Test duration is required"
        } else {
          if (text.value.matches("[0-9]* (second|minute|hour)s?")) {
            styleClass += "ok"
            durationErrorLabel.text = ""
          } else {
            if (!styleClass.contains("error"))
              styleClass += "error"
            durationErrorLabel.text = "Invalid duration"
            styleClass -= "ok"
          }
        }
      }
    })
  }

  val intervalErrorLabel: Label = new Label()
  val intervalText: TextField = new PersistentPromptTextField("", "20 seconds") {

    focused.onChange((obs, oldValue, newValue) => {
      if (oldValue) {
        if (text.value.isEmpty) {
          if (!styleClass.contains("error"))
            styleClass += "error"
          styleClass -= "ok"
          intervalErrorLabel.text = "Test interval is required"
        } else {
          if (text.value.matches("[0-9]* (second|minute|hour)s?")) {
            styleClass += "ok"
            intervalErrorLabel.text = ""
          } else {
            if (!styleClass.contains("error"))
              styleClass += "error"
            intervalErrorLabel.text = "Invalid interval"
            styleClass -= "ok"
          }
        }
      }
    })
  }

  val timeoutErrorLabel: Label = new Label()
  val timeoutText: TextField = new PersistentPromptTextField("", "1 second") {

    focused.onChange((obs, oldValue, newValue) => {
      if (oldValue) {
        if (text.value.isEmpty) {
          if (!styleClass.contains("error"))
            styleClass += "error"
          styleClass -= "ok"
          timeoutErrorLabel.text = "Connection timeout is required"
        } else {
          if (text.value.matches("[0-9]* (millisecond|second|minute|hour)s?")) {
            styleClass += "ok"
            timeoutErrorLabel.text = ""
          } else {
            if (!styleClass.contains("error"))
              styleClass += "error"
            timeoutErrorLabel.text = "Invalid connection timeout"
            styleClass -= "ok"
          }
        }
      }
    })
  }


  val textFields: List[TextField] = List(nodeText, portText, durationText, intervalText, timeoutText)
  val labelFieldErrors: List[Label] = List(nodeErrorLabel, portErrorLabel, durationErrorLabel, intervalErrorLabel, intervalErrorLabel, timeoutErrorLabel)

  val submitBtn: Button = new Button("Submit") {
    font = Font.font("ubuntu", 14)
    onAction = handle {

      textFields.count(_.text.value.isEmpty) match {
        case 0 =>
          Try {
            theController.getHostAddress(nodeText.text.value)
          } match {
            case Success(e) => {
              theController.submitJob(e,
                portText.text.value.toInt,
                Duration(durationText.text.value),
                Duration(intervalText.text.value),
                Duration(timeoutText.text.value),
                concurrentTask.value.toInt
              )
              textFields.foreach(_.text = "")
              textFields.filter(_.styleClass.contains("error")).foreach(_.styleClass.replaceAll("error", ""))
              textFields.filter(_.styleClass.contains("ok")).foreach(_.styleClass.replaceAll("ok", ""))
              labelFieldErrors.foreach(_.text = "")
              concurrentTask.value = 0.0
            }
            case Failure(ex) => {
              nodeErrorLabel.text = ex.getMessage
              if (nodeText.styleClass.contains("ok"))
                nodeText.styleClass -= "ok"
              if (!nodeText.styleClass.contains("error"))
                nodeText.styleClass += "error"
            }
          }
        case _ => textFields.filter(_.text.value.isEmpty).filter(_.styleClass.contains("error")).foreach(_.styleClass += "error")
      }

    }

  }
  val resetBtn: Button = new Button("Reset") {
    font = Font.font("ubuntu", 14)
    onAction = handle {
      textFields.foreach(_.text = "")
      textFields.filter(_.styleClass.contains("error")).foreach(_.styleClass -= "error")
      textFields.filter(_.styleClass.contains("ok")).foreach(_.styleClass -= "ok")
      labelFieldErrors.foreach(_.text = "")
    }
  }

  stage = new JFXApp.PrimaryStage {
    title = AppConfig.appName
    minHeight = 450
    minWidth = 700
    scene = new Scene(700, 450) {
      stylesheets = List("gui.css")
      root = new BorderPane {
        center = createTabPane
      }
    }
  }


  private def createTabPane: TabPane = {

    val tabMenu2 = new Tab {
      graphic = new ImageView(new Image("images/svg/internet.png"))
      tooltip = Tooltip("Home")
      closable = false
      content = new BorderPane {
        padding = Insets(20)
        top = new VBox {
          padding = Insets(0, 0, 20, 0)
          children = Seq(new Label {
            text = "Real Time TCP test tool"
            minHeight = 16
            font = Font.font("ubuntu", 24)
          }, new Separator())
        }
        left = new GridPane {
          hgap = 10
          vgap = 15
          padding = Insets(0, 0, 20, 15)
          add(new Label("Active tests:"), 0, 0)
          add(new Label() {
            text <== theController.activeObserver
          }, 1, 0)

          add(new Label("Completed tests:"), 0, 1)
          add(new Label() {
            text <== theController.totalObserver
          }, 1, 1)
        }
      }
    }

    val tabMenu = new Tab {
      graphic = new ImageView(new Image("images/svg/001-business.png"))
      tooltip = Tooltip("Create new test")
      closable = false
      content = new BorderPane {
        padding = Insets(20)
        top = new VBox {
          padding = Insets(0, 0, 20, 0)
          children = Seq(new Label {
            text = "New Test"
            minHeight = 16
            font = Font.font("ubuntu", 24)
          }, new Separator())
        }
        center = new GridPane {
          hgap = 10
          vgap = 15
          padding = Insets(0, 0, 20, 15)
          add(new Label("Node:") {
            font = Font.font("ubuntu", 16)
          }, 0, 0)
          add(nodeText, 1, 0)
          add(nodeErrorLabel, 2, 0)

          add(new Label("Port:") {
            font = Font.font("ubuntu", 16)
          }, 0, 1)
          add(portText, 1, 1)
          add(portErrorLabel, 2, 1)

          add(new Label("Duration:") {
            font = Font.font("ubuntu", 16)
          }, 0, 2)
          add(durationText, 1, 2)
          add(durationErrorLabel, 2, 2)

          add(new Label("Interval:") {
            font = Font.font("ubuntu", 16)
          }, 0, 3)
          add(intervalText, 1, 3)
          add(intervalErrorLabel, 2, 3)

          add(new Label("Timeout:") {
            font = Font.font("ubuntu", 16)
          }, 0, 4)
          add(timeoutText, 1, 4)
          add(timeoutErrorLabel, 2, 4)

          add(new Label("Concurrent:") {
            font = Font.font("ubuntu", 16)
          }, 0, 5)
          add(concurrentTask, 1, 5)
          add(concurrentLabel, 2, 5)
        }

        bottom = new HBox {
          alignment = Pos.BaselineLeft
          spacing = 10
          children = Seq(resetBtn, submitBtn)
        }
      }
    }

    val tabMenu1 = new Tab {
      graphic = new ImageView(new Image("images/svg/002-analytics-1.png"))
      tooltip = Tooltip("Table results")
      closable = false
      content = new BorderPane {
        padding = Insets(20)
        top = new VBox {
          padding = Insets(0, 0, 20, 0)
          children = Seq(new Label {
            text = "Results"
            minHeight = 16
            font = Font.font("ubuntu", 24)
          }, new Separator())
        }
        center = new ScrollPane {
          content = table
          fitToHeight = true
        }
        bottom = new HBox {
          alignment = Pos.Center
          padding = Insets(10, 0, 0, 0)
          children = Seq(new Button("Export Table") {
            onAction = handle {
              val dc = new DirectoryChooser {
                title = "Select destination folder"
              }

              val file = dc.showDialog(stage)

              if (file != null) {
                if (file.isDirectory && file.canWrite) {
                  theController.writeTo(file.getAbsolutePath, writeResult)
                } else {
                  new Alert(AlertType.Error) {
                    initOwner(stage)
                    title = "Export table"
                    headerText = "Invalid directory."
                    contentText = "Ooops, you don't have permissions.\nSelect another directory, please!"
                  }.showAndWait()
                }
              }
            }
          })
        }
      }
    }


    new TabPane {
      side = Side.Bottom
      tabs = Seq(tabMenu2, tabMenu, tabMenu1)
    }
  }

  stage.onCloseRequest = (e: WindowEvent) => {

    if (theController.activeObserver.value != "0") {

      val alert = new Alert(AlertType.Confirmation) {
        initOwner(stage)
        title = "Confirmation Dialog"
        headerText = "Look, you have tests running."
        contentText = "Are you ok leaving?"
      }

      val result = alert.showAndWait()

      result match {
        case Some(ButtonType.OK) => theController.shutdown
        case _ => e.consume()
      }
    } else {
      theController.shutdown
    }
  }


  def writeResult(boolean: Boolean): Unit = {
    Platform.runLater {
      new Alert(AlertType.Information) {
        initOwner(stage)
        title = "Export table"
        headerText = "Your file was written with success"
      }.showAndWait()
    }
  }


  private def createResultsTableNode(): Node = {
    // Create columns
    val dateStartColumn = new TableColumn[TableResultsModel, String] {
      text = "Start"
      cellValueFactory = {
        _.value.start
      }
      prefWidth = 140
    }

    val dateEndColumn = new TableColumn[TableResultsModel, String] {
      text = "End"
      cellValueFactory = {
        _.value.end
      }
      prefWidth = 140
    }

    val nodeColumn = new TableColumn[TableResultsModel, String] {
      text = "Node"
      cellValueFactory = {
        _.value.host
      }
      prefWidth = 140
    }

    val portColumn = new TableColumn[TableResultsModel, Int] {
      text = "Port"
      cellValueFactory = {
        _.value.port
      }
      prefWidth = 140
    }

    val durationColumn = new TableColumn[TableResultsModel, String] {
      text = "Duration"
      cellValueFactory = {
        _.value.duration
      }
      prefWidth = 140
    }

    val intervalColumn = new TableColumn[TableResultsModel, String] {
      text = "Interval"
      cellValueFactory = {
        _.value.interval
      }
      prefWidth = 140
    }

    val timeOutColumn = new TableColumn[TableResultsModel, String] {
      text = "Timeout"
      cellValueFactory = {
        _.value.timeout
      }
      prefWidth = 140
    }

    val succeededColumn = new TableColumn[TableResultsModel, Int] {
      text = "Succeeded"
      cellValueFactory = {
        _.value.succeeded
      }
      prefWidth = 140
    }

    val failedColumn = new TableColumn[TableResultsModel, Int] {
      text = "Failed"
      cellValueFactory = {
        _.value.failed
      }
      prefWidth = 140
    }

    val totalColumn = new TableColumn[TableResultsModel, Int] {
      text = "Total"
      cellValueFactory = {
        _.value.total
      }
      prefWidth = 140
    }

    val availabilityColumn = new TableColumn[TableResultsModel, Double] {
      text = "Availability"
      cellValueFactory = {
        _.value.available
      }
      prefWidth = 140
    }

    val mtbfColumn = new TableColumn[TableResultsModel, Long] {
      text = "MTBF"
      cellValueFactory = {
        _.value.mtbf
      }
      prefWidth = 140
    }

    // Create table
    val table = new TableView[TableResultsModel](theController.resultsMembers) {
      columns += (dateStartColumn,
        dateEndColumn,
        nodeColumn,
        portColumn,
        durationColumn,
        intervalColumn,
        timeOutColumn,
        succeededColumn,
        failedColumn,
        totalColumn,
        availabilityColumn,
        mtbfColumn
      )
    }

    table
  }

}

class PersistentPromptTextField(text: String, prompt: String) extends TextField() {
  super.text.value = text
  super.promptText.value = prompt
  styleClass += "persistent-prompt"
  refreshPromptVisibility()

  super.text.onChange((obs, old, n) => {
    refreshPromptVisibility()
  })


  private def refreshPromptVisibility() = {
    val text = super.text.value
    if (isEmptyString(text)) styleClass -= "no-prompt"
    else if (!styleClass.contains("no-prompt")) styleClass += "no-prompt"
  }

  private def isEmptyString(text: String) = text == null || text.isEmpty

}