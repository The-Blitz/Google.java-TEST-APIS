import com.google.api.services.classroom.Classroom;
import com.google.api.services.admin.directory.Directory;

import java.io.IOException;
import java.security.GeneralSecurityException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.*;
import com.google.api.services.classroom.model.*;
import com.google.api.services.admin.directory.model.*;

public class Main {

    private static String objectToJson(Object objeto) throws JsonProcessingException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(objeto);
    }

    private static void demo(Classroom servicioClase) throws IOException{
        List<String> nombresClases = new ArrayList<>();
        List<String> nombresTopicos= new ArrayList<>();
        List<String> nombresTareas = new ArrayList<>();

        nombresClases.add("Clase de Prueba 1");
        nombresTopicos.add("Topico 1");
        nombresTareas.add("Tarea 1");
        List<String> idClases = ClassroomJavaAPI.cargaMasiva(servicioClase,nombresClases,nombresTopicos,nombresTareas);


        ClassroomJavaAPI.borradoMasivo(servicioClase,idClases);

    }

    public static void main(String... args) throws IOException, GeneralSecurityException {

        Classroom servicioClase = ClassroomJavaAPI.obtenerServicio();
        Directory servicioUsuario = GsuiteJavaAPI.obtenerServicio();

        demo(servicioClase);

    }
}
