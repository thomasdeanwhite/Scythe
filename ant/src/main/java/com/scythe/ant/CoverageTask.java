package com.scythe.ant;

import com.scythe.Agent;
import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.RuntimeConfigurable;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;
import org.apache.tools.ant.UnknownElement;

public class CoverageTask extends Task implements TaskContainer{

  private Task testTask;
  private File outputFile;

  public void execute() throws BuildException{
    validate();
    testTask.execute();
  }

  private void validate(){
    if(outputFile == null){
      throw new BuildException("Output File is not set");
    }
  }



  public void setOutputfile(File outputFile){
    this.outputFile = outputFile;
  }


  @Override
  public void addTask(Task task) {
    if(testTask != null){
      throw new BuildException("This task can only take one child task");
    }
    this.testTask = task;
    final UnknownElement el = new UnknownElement("jvmarg");
    el.setTaskName("jvmarg");
    el.setQName("jvmarg");

    final RuntimeConfigurable runtimeConfigurableWrapper = el.getRuntimeConfigurableWrapper();
    try{
      File agentFile = Agent.extractToTempLocation();
      runtimeConfigurableWrapper.setAttribute("value", "-javaagent:"+ agentFile.getAbsolutePath()+"=\"-instrument true -coverage_on_exit true -coverage_file "+outputFile.getAbsolutePath()+"\"");
      task.getRuntimeConfigurableWrapper().addChild(runtimeConfigurableWrapper);

      ((UnknownElement) task).addChild(el);

    }catch(IOException e){
      e.printStackTrace();
    }
    testTask.maybeConfigure();
  }
}
