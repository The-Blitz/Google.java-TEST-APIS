import com.google.api.services.classroom.Classroom;
import com.google.api.services.admin.directory.Directory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.*;
import com.google.api.services.classroom.model.*;
import com.google.api.services.admin.directory.model.*;
import javafx.util.Pair;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;


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

    private static void readCSV(String filePath) throws IOException {
        InputStream in = Main.class.getResourceAsStream(filePath);
        Reader reader = new InputStreamReader(in);
        CSVParser csvParser = new CSVParser(reader, CSVFormat.EXCEL.withHeader("persona_documento_numero" ,
                "a_paterno", "a_materno", "nombre_completo", "persona_correo") );

        for (CSVRecord csvRecord : csvParser) {
            // Accessing Values by Column Index
            String dni = csvRecord.get(0);
            /*
            String apaterno = csvRecord.get(1);
            String amaterno = csvRecord.get(2);
            String nombre = csvRecord.get(3);
            String correo = csvRecord.get(4);
            */
            System.out.println("Record No - " + csvRecord.getRecordNumber());
            System.out.println("---------------");
            System.out.println("Dni : " + dni);/*
            System.out.println("apaterno : " + apaterno);
            System.out.println("amaterno : " + amaterno);
            System.out.println("nombre : " + nombre);
            System.out.println("correo : " + correo);*/
            System.out.println("---------------\n\n");
        }

    }

    public static void main(String... args) throws IOException, GeneralSecurityException {

        Classroom servicioClase = ClassroomJavaAPI.obtenerServicio();
        Directory servicioUsuario = GsuiteJavaAPI.obtenerServicio();

        //demoClase(servicioClase);
        //demoUsuario(servicioUsuario);

    }
}
