import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.classroom.ClassroomScopes;
import com.google.api.services.classroom.model.*;
import com.google.api.services.classroom.Classroom;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.lang.reflect.Method;

public class ClassroomJavaAPI {
    private static final String APPLICATION_NAME = "Google Classroom API Java";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "classroom-tokens";

    /**
     * Los SCOPES son los permisos que se le da al servicio, dependiendo de lo que se necesite hacer.
     * Recuerde borrar los tokens en caso modifique los SCOPES
     */
    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/classroom.courses", "https://www.googleapis.com/auth/classroom.coursework.students",
            "https://www.googleapis.com/auth/classroom.rosters","https://www.googleapis.com/auth/classroom.announcements",
            "https://www.googleapis.com/auth/classroom.topics"
    );
    private static final String CREDENTIALS_FILE_PATH = "/classroom-credentials.json";

    /**
     * Se crea un objeto de credencial autorizada
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Cargando credenciales
        InputStream in = ClassroomJavaAPI.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Se construye el flujo y se activa la solicitud de atencion del usuario
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    //Imprime las primeras clases(depende de la cantidad) a las que se tienen accesso
    public static void listarClases(Classroom servicio , Integer cantidadClases) throws IOException{
        ListCoursesResponse response = servicio.courses().list().setPageSize(cantidadClases).execute();
        List<Course> clases = response.getCourses();
        if (clases == null || clases.size() == 0) {
            System.out.println("No se encontraron clases");
        } else {
            System.out.println("Courses:");
            for (Course clase : clases) {
                System.out.printf("%s %s\n", clase.getName() , clase.getId());
            }
        }
    }

    //Se crea una clase, solo debe enviarle un nombre
    private static Course crearClase(Classroom servicio , String nombre) throws IOException{
        Course clase = new Course();

        clase.set("name", nombre);
        clase.set("ownerId", "me");
        clase.set("courseState", "ACTIVE");

        clase = servicio.courses().create(clase).execute();
        System.out.println(java.text.MessageFormat.format( "Clase creada: {0} con ID {1}" , clase.getName(), clase.getId() ));
        return clase;
    }

    private static Course obtenerClaseporId(Classroom servicio , String id) throws IOException {
        return servicio.courses().get(id).execute();
    }
    // Se elimina una clase(antes de eliminar debe estar archivada)
    // TODO: archivar una clase
    private static void borrarClase(Classroom servicio , String id) throws IOException {
        servicio.courses().delete(id).execute();
        System.out.println(java.text.MessageFormat.format( "La clase: {0} fue eliminada" , id ));
    }


    //Se crea un nuevo topico, y se le agrega a una clase usando el id de la clase y el nombre del nuevo topico
    // TODO: eliminar Topico de Clase
    private static Topic agregarTopicoaClase(Classroom servicio , String idClase ,String nombreTopico ) throws IOException{
        Topic topico = new Topic();
        topico.set("name", nombreTopico);

        topico = servicio.courses().topics().create(idClase,topico).execute();
        System.out.println(java.text.MessageFormat.format( "Se Creo el topico con id {0} para el curso con id {1}" , topico.getTopicId(), idClase ));
        return topico;
    }

    //Se crea una nueva tarea para algun topico perteneciente a una clase.
    //TODO: eliminar tarea de clase
    private static CourseWork agregarTareaaTopico(Classroom servicio , String idClase , String idTopico, String tituloTarea ,String tipoTarea ) throws IOException{
        CourseWork tarea = new CourseWork();
        tarea.set("title", tituloTarea);
        tarea.set("workType", tipoTarea); // EL tipo de tarea puede ser: ASSIGNMENT,SHORT_ANSWER_QUESTION o MULTIPLE_CHOICE_QUESTION
        tarea.set("topicId", idTopico);
        tarea.set("state", "DRAFT"); // EL estado de la tarea puede ser: DRAFT o PUBLISHED

        tarea  = servicio.courses().courseWork().create(idClase,tarea).execute();
        System.out.println(java.text.MessageFormat.format( "Se Creo la tarea con id {0} para el topico con id {1}" , tarea.getId(), tarea.getTopicId() ));
        return tarea;
    }

    //Se agregar un profesor, solo con su correo a la clase indicada, mediante su ID
    private static Teacher agregarProfesoraClase(Classroom servicio, String emailProfesor, String idClase) throws IOException{
        Teacher profesor = new Teacher();
        profesor.setUserId(emailProfesor);

        profesor  = servicio.courses().teachers().create(idClase,profesor).execute();

        System.out.println(java.text.MessageFormat.format( "Se agrego al profesor con id {0} a la clase con id {1}" , profesor.getUserId(), profesor.getCourseId() ));

        return profesor;
    }

    //Se invita a una persona a una clase de acuerdo a su rol(puede ser alumno o profesor)
    private static void invitarPersonaaClase(Classroom servicio, String email, String idClase, String tipo ) throws IOException{
        // el tipo debe ser o "STUDENT" o TEACHER"

        Invitation invitacion = new Invitation();
        invitacion.setUserId(email);
        invitacion.setRole(tipo);
        invitacion.setCourseId(idClase);

        invitacion  = servicio.invitations().create(invitacion).execute();

        System.out.println(java.text.MessageFormat.format( "Se invito a la persona con id {0} a la clase con id {1}" , invitacion.getUserId(), invitacion.getCourseId() ));

    }

    //Generar varias clases con varios topicos y tareas
    private static void cargaMasiva(Classroom servicio, List<String> listaClases, List<String> listaTopicos , List<String> listaTareas) throws IOException {
        for (String nombreClase : listaClases) {
            Course claseActual = crearClase(servicio , nombreClase);
            for (String nombreTopico : listaTopicos) {
                Topic nuevoTopico = agregarTopicoaClase(servicio,claseActual.getId(),nombreTopico);
                for(String nombreTarea : listaTareas){
                    agregarTareaaTopico(servicio,claseActual.getId(),nuevoTopico.getTopicId(),nombreTarea,"ASSIGNMENT");
                }
            }
        }

    }

    public static Classroom obtenerServicio() throws IOException,GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new Classroom.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT)).setApplicationName(APPLICATION_NAME).build();
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {

        Classroom servicio = obtenerServicio();

        //listarClases(servicio,5);

        //agregarTopicoaClase(servicio, "62895258692","Algebra" );
        //agregarTareaaTopico(servicio,"62895258692","64703673742","Tarea 2", "ASSIGNMENT");
    }
}