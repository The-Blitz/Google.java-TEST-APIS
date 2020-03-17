import com.google.api.services.classroom.Classroom;
import com.google.api.services.admin.directory.Directory;

import java.io.*;
import java.security.GeneralSecurityException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.*;
import com.google.api.services.classroom.model.*;
import com.google.api.services.admin.directory.model.*;
import javafx.util.Pair;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;


public class Main {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    // NOMBRECLASE -> TOPICO -> TAREA
    private static HashMap< String, HashMap< String , HashSet<String> > > estructuraClases = new HashMap<>();
    //NOMBRECLASE -> IDCLASE
    private static HashMap< String, HashSet<String> > clasesPorNombre  = new HashMap<>();
    //NOMBRETOPICO -> IDCLASE-> IDTOPICO
    private static HashMap< String, HashMap<String,String > > topicosPorNombre = new HashMap<>();
    //NOMBREA TAREA -> IDTOPICO -> IDTAREA
    private static HashMap< String, HashMap<String,String > > tareasPorNombre = new HashMap<>();

    private static Course buscarClase(Classroom servicioClase, String nombreClase){
        try{
            return ClassroomJavaAPI.obtenerClaseporId(servicioClase , clasesPorNombre.get(nombreClase).stream().findFirst().get() );
        }
        catch(Exception e){
            LOGGER.log(Level.WARNING , "La clase dada no pudo encontrarse");
            return new Course();
        }
    }

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
            //Obteniendo valores por columna
            String dni = csvRecord.get(0);
            String apaterno = csvRecord.get(1);
            String amaterno = csvRecord.get(2);
            String nombre = csvRecord.get(3);
            String correo = csvRecord.get(4);

            System.out.println("Record No - " + csvRecord.getRecordNumber());
            System.out.println("---------------");
            System.out.println("Dni : " + dni);
            System.out.println("apaterno : " + apaterno);
            System.out.println("amaterno : " + amaterno);
            System.out.println("nombre : " + nombre);
            System.out.println("correo : " + correo);
            System.out.println("---------------\n\n");
        }

    }

    private static void cargarClases(Classroom servicioClase) throws IOException{
        List<Course> listaClases = ClassroomJavaAPI.listarClases(servicioClase,ClassroomJavaAPI.totaldeClases(servicioClase) );
        for (Course clase : listaClases) {
            if( ! Objects.equals(clase.getName(),"Clase de Prueba 1")) continue;

            List<Topic> listaTopicos = ClassroomJavaAPI.obtenerTopicosdeClase(servicioClase, clase.getId() , ClassroomJavaAPI.totaldeTopicosdeClase(servicioClase, clase.getId()));
            List<CourseWork> listaTareas = ClassroomJavaAPI.obtenerTareasdeClase(servicioClase, clase.getId() , ClassroomJavaAPI.totaldeTareasdeClase(servicioClase, clase.getId()));

            if(!estructuraClases.containsKey(clase.getName())) estructuraClases.put(clase.getName()  , new HashMap<>() ) ;
            if(clasesPorNombre.isEmpty()) clasesPorNombre.put(clase.getName(),  new HashSet<>() );
            clasesPorNombre.get(clase.getName()).add(clase.getId());

            for(Topic topico: listaTopicos){
                if(!estructuraClases.get(clase.getName()).containsKey(topico.getName()))
                    estructuraClases.get(clase.getName()).put(topico.getName(), new HashSet<>());
                if(topicosPorNombre.isEmpty()) topicosPorNombre.put(topico.getName(),  new HashMap<>() );
                topicosPorNombre.get(topico.getName()).put(clase.getId(),topico.getTopicId());

                for(CourseWork tarea: listaTareas){
                    if(Objects.equals(tarea.getState(),"DRAFT")) continue;
                    if(!estructuraClases.get(clase.getName()).get(topico.getName()).contains(tarea.getTitle()) )
                        estructuraClases.get(clase.getName()).get(topico.getName()).add(tarea.getTitle());
                    if(tareasPorNombre.isEmpty()) tareasPorNombre.put(tarea.getTitle(),  new HashMap<>() );
                    tareasPorNombre.get(tarea.getTitle()).put(topico.getTopicId(),tarea.getId());
                }
            }

        }

    }

    private static List<String> listaCiencias = new ArrayList<>();
    private static List<String> listaLetras = new ArrayList<>();

    private static List<Pair<String,String>> listadeTopicos(){
        List<Pair<String,String>> clases = new ArrayList<>();

        clases.add(new Pair<>("Ciencias", "Algebra") );
        clases.add(new Pair<>("Ciencias", "Aritmética") );
        clases.add(new Pair<>("Ciencias", "Biología") );
        clases.add(new Pair<>("Ciencias", "Física") );
        clases.add(new Pair<>("Ciencias", "Geometría") );
        clases.add(new Pair<>("Ciencias", "Química") );
        clases.add(new Pair<>("Ciencias", "Raz. Mat") );
        clases.add(new Pair<>("Ciencias", "Trigonometria") );

        clases.add(new Pair<>("Letras", "Geografía") );
        clases.add(new Pair<>("Letras", "Historia del Perú") );
        clases.add(new Pair<>("Letras", "Inglés") );
        clases.add(new Pair<>("Letras", "Lenguaje") );
        clases.add(new Pair<>("Letras", "Literatura") );
        clases.add(new Pair<>("Letras", "Raz. Verbal") );
        clases.add(new Pair<>("Letras", "Teatro y Orat") );

        listaCiencias.add("HELICOTEORIA - CAPITULO 01");
        listaCiencias.add("HELICOPRACTICA - CAPITULO 01");
        listaCiencias.add("HELICOTALLER - PROBLEMAS - CAPITULO 01");
        listaCiencias.add("HELICOTALLER - SOLUCIONARIO - CAPITULO 01");
        listaCiencias.add("HELICOTAREA");
        listaCiencias.add("RESOLUCION DE LA HELICOTAREA");

        listaLetras.add("HELICOTEORIA - HELICOPRACTICA - CAPITULO 01");
        listaLetras.add("HELICOTALLER - PREGUNTAS - CAPITULO 01");
        listaLetras.add("HELICOTALLER - SOLUCIONARIO - CAPITULO 01");
        listaLetras.add("HELICOTAREA");
        listaLetras.add("RESOLUCION DE LA HELICOTAREA");

        return clases;
    }

    private static void llenarClase(Classroom servicioClase, String nombreClase ) throws IOException{

        List<Pair<String,String>> nombresTopicos = listadeTopicos();

        for( String claseId : clasesPorNombre.get(nombreClase)){
            Course clase = ClassroomJavaAPI.obtenerClaseporId(servicioClase,claseId);

            for(Pair<String,String> parTopico: nombresTopicos){
                String tipo = parTopico.getKey();
                String nombre = parTopico.getValue();

                if(estructuraClases.get(nombreClase).containsKey(nombre)) {

                    if(topicosPorNombre.get(nombre).containsKey(claseId)){
                        Topic topico = ClassroomJavaAPI.obtenerTopicoconIds(servicioClase, claseId,
                                topicosPorNombre.get(nombre).get(claseId) );

                        if(Objects.equals(tipo,"Ciencias")) {

                            for(String nuevaTarea : listaCiencias){
                                if ( (!estructuraClases.get(nombreClase).get(nombre).contains(nuevaTarea)) || (!tareasPorNombre.get(nuevaTarea).containsKey(topico.getTopicId())) )
                                    ClassroomJavaAPI.agregarTareaaTopico(servicioClase, claseId, topico.getTopicId(), nuevaTarea, "ASSIGNMENT");
                            }
                        }

                        else if(Objects.equals(tipo,"Letras")){
                            for(String nuevaTarea : listaLetras){
                                if ((!estructuraClases.get(nombreClase).get(nombre).contains(nuevaTarea)) || (!tareasPorNombre.get(nuevaTarea).containsKey(topico.getTopicId())) )
                                    ClassroomJavaAPI.agregarTareaaTopico(servicioClase, claseId, topico.getTopicId(), nuevaTarea, "ASSIGNMENT");
                            }
                        }
                    }

                }

            }

        }


    }

    private static void readTXT(Classroom servicioClase,String filePath, String nombreClase) throws IOException {
        File archivo = new File(filePath);
        Scanner lector = new Scanner(archivo);

        //Course clase = ClassroomJavaAPI.obtenerClaseporId(servicioClase,"53401726393");
        //Course clase = ClassroomJavaAPI.obtenerClaseporNombre(servicioClase,nombreClase);
        Course clase = buscarClase(servicioClase,nombreClase);

        if(Objects.equals(clase.getId(),null)){
            LOGGER.log(Level.WARNING,java.text.MessageFormat.format( "La clase de nombre {0} no existe " , nombreClase ));
            return;
        }

        while (lector.hasNextLine()) {
            String email = lector.nextLine();
            try{
                ClassroomJavaAPI.agregarAlumnoaClase(servicioClase,email,clase.getId(),clase.getEnrollmentCode());
            }
            catch (Exception e){
                LOGGER.log(Level.WARNING,java.text.MessageFormat.format( "No se puede agregar al alumno con correo {0} . O el alumno ya esta en la clase o el correo no es valido" , email ));
            }
        }
        lector.close();
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {

        Classroom servicioClase = ClassroomJavaAPI.obtenerServicio();
        Directory servicioUsuario = GsuiteJavaAPI.obtenerServicio();

        //cargarClases(servicioClase);
        //llenarClase(servicioClase,"Clase de Prueba 1");

    }
}
