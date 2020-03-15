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
import javafx.util.Pair;

public class Main {

    private static String objectToJson(Object objeto) throws JsonProcessingException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(objeto);
    }

    private static void demoClase(Classroom servicioClase) throws IOException{
        List<String> nombresClases = new ArrayList<>();
        List<String> nombresTopicos= new ArrayList<>();
        List<String> nombresTareas = new ArrayList<>();

        nombresClases.add("Clase de Prueba 1");
        nombresTopicos.add("Topico 1");
        nombresTareas.add("Tarea 1");
        List<String> idClases = ClassroomJavaAPI.cargaMasiva(servicioClase,nombresClases,nombresTopicos,nombresTareas);

        //ClassroomJavaAPI.borradoMasivo(servicioClase,idClases);

    }

    private static void demoUsuario(Directory servicioUsuario) throws IOException{
        List<Pair<Character,String>> listaGrupos = new ArrayList<>();
        Pair<Character,String> par1 = new Pair<>('a', "ALUMNOS_FRANQUICIA");
        Pair<Character,String> par2 = new Pair<>('e', "ALUMNOS_COLEGIO");
        //listaGrupos.add()
        GsuiteJavaAPI.crearUsuario(servicioUsuario,"a00000007@sacooliveros.edu.pe", "Diaz","Marco",listaGrupos);
        GsuiteJavaAPI.borrarUsuario(servicioUsuario,"a00000007@sacooliveros.edu.pe" );
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {

        Classroom servicioClase = ClassroomJavaAPI.obtenerServicio();
        Directory servicioUsuario = GsuiteJavaAPI.obtenerServicio();

        //demoClase(servicioClase);
        //demoUsuario(servicioUsuario);



    }
}
