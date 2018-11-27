package com.scythe.instrumenter.analysis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.scythe.instrumenter.InstrumentationProperties;
import com.scythe.util.Util;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CoverageSerializer extends TypeAdapter<ClassAnalyzer> {
  private Type classType = new TypeToken<Class<?>>(){}.getType();
  private GsonBuilder variableWriter = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT)
      .serializeNulls().registerTypeAdapter(classType, new TypeAdapter<Class>(){

    @Override
    public void write(JsonWriter jsonWriter, Class aClass) throws IOException {
      jsonWriter.value(aClass.getName());
    }

    @Override
    public Class read(JsonReader jsonReader) throws IOException {
      try {
        return Class.forName(jsonReader.nextString());
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
      return null;
    }
  });
  private final Gson gson;

  public CoverageSerializer(){
    if(InstrumentationProperties.PRETTY_PRINT_COVERAGE){
      variableWriter.setPrettyPrinting();
    }
    if(InstrumentationProperties.DISABLE_HTML_ESCAPE){
      variableWriter.disableHtmlEscaping();
    }
    gson = variableWriter.create();
  }

  @Override
  public void write(JsonWriter out, ClassAnalyzer classAnalyzer) throws IOException {
    try {
      out.beginArray();

      List<Field> fields = Arrays.asList(ClassAnalyzer.class.getDeclaredFields()).stream().filter(f -> !f.getName().equals("out")).collect(
          Collectors.toList());
      for (Field f : fields) {
        if(f.getName().contains("jacoco")){
          continue;
        }
        f.setAccessible(true);
        Type t = f.getGenericType();
        out.beginObject();
        out.name(f.getName());
        String json = gson.toJson(f.get(null), t);
        out.value(json);
        out.endObject();
      }
      out.endArray();
      out.flush();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  @Override
  public ClassAnalyzer read(JsonReader in) throws IOException {
    try{
      in.beginArray();
      List<Field> fields = Arrays.asList(ClassAnalyzer.class.getDeclaredFields()).stream().filter(f -> !f.getName().equals("out")).collect(
          Collectors.toList());
      while(in.hasNext()){
        in.beginObject();
        String name = in.nextName();
        Field f = ClassAnalyzer.class.getDeclaredField(name);
        f.setAccessible(true);
        Type t = f.getGenericType();
        Object json = gson.fromJson(in.nextString(), t);
        in.endObject();
        if(Modifier.isFinal(f.getModifiers())){
          continue;
        }
        Util.setField(f, json);
      }
      in.endArray();
      }catch(IllegalAccessException e){
      e.printStackTrace();
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    }
    return null;
  }
}
