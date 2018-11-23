package com.scythe.ant;

import com.google.gson.Gson;
import com.scythe.instrumenter.analysis.ClassAnalyzer;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTask;
import org.apache.tools.ant.types.Path;

public class CoverageTask extends Task implements TaskContainer{

  private List<Path> paths = new ArrayList<>();
  private List<Task> tests = new ArrayList<>();

  private File outputFile;

  public void execute() throws BuildException{
    validate();
    tests.forEach(t -> System.out.println(t.getTaskType()));
    tests.forEach(Task::execute);
    writeCoverage();
  }

  private void writeCoverage(){
    Map<String, Set<Integer>> covered = new HashMap<>();
    ClassAnalyzer.getLinesCovered().forEach(line -> {
      String className = line.getLine().getClassName();
      if (!covered.containsKey(className)){
        covered.put(className, new HashSet<>());
      }
      covered.get(className).add(line.getLine().getGoalId());
    });
    Gson gson = new Gson();
    String out = gson.toJson(covered);
    try {
      BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile));
      os.write(out.getBytes());
      os.flush();
      os.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private void validate(){
    if(outputFile == null){
      throw new BuildException("Output File is not set");
    }
    if(tests.size() < 1){
      throw new BuildException("There are no tests to run!");
    }
  }


  @Override
  public void addTask(Task task) {
    tests.add(task);
  }

  public void addPath(Path path){
    paths.add(path);
  }

  public void setOutputfile(File outputFile){
    this.outputFile = outputFile;
  }


}
