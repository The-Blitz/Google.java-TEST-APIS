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
import sun.rmi.runtime.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.*;
import java.lang.reflect.Method;

import java.util.logging.Logger;
import java.util.logging.Level;

public class ClassroomJavaAPI {
    private static final String APPLICATION_NAME = "Google Classroom API Java";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "classroom-tokens";
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /**
     * Los SCOPES son los permisos que se le da al servicio, dependiendo de lo que se necesite hacer.
     * Recuerde borrar los tokens en caso modifique los SCOPES
     */
    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/classroom.courses", "https://www.googleapis.com/auth/classroom.coursework.students",
            "https://www.googleapis.com/auth/classroom.rosters","https://www.googleapis.com/auth/classroom.announcements",
            "https://www.googleapis.com/auth/classroom.topics", "https://www.googleapis.com/auth/classroom.profile.emails",
            "https://www.googleapis.com/auth/classroom.profile.photos"
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

    public static int totaldeClases(Classroom servicio) throws IOException{
        try {
            return servicio.courses().list().size();
        }
        catch (Exception e){
            LOGGER.log(Level.WARNING, "No hay clases registradas");
            return 0;
        }
    }

    public static List<Course> listarClases(Classroom servicio , Integer cantidadClases) throws IOException{
        try {
            ListCoursesResponse response = servicio.courses().list().setPageSize(cantidadClases).execute();
            if(response.isEmpty()) {
                LOGGER.log(Level.WARNING, "No hay clases registradas");
                return new ArrayList<>();
            }
            return response.getCourses();
        }
        catch (Exception e){
            LOGGER.log(Level.WARNING, "Hubo un error con el servicio");
            return new ArrayList<>();
        }

    }

    //Se crea una clase, solo debe enviarle un nombre
    public static Course crearClase(Classroom servicio , String nombre) throws IOException{
        Course clase = new Course();

        clase.set("name", nombre);
        clase.set("ownerId", "me");
        clase.set("courseState", "ACTIVE");
        try {
            clase = servicio.courses().create(clase).execute();
            LOGGER.log(Level.INFO, java.text.MessageFormat.format("Clase creada: {0} con ID {1}", clase.getName(), clase.getId()));
        }
        catch (Exception e){
            LOGGER.log(Level.WARNING,"La clase no pudo ser creada" );
        }
        return clase;
    }

    //Obtener una clase dando su Id
    public static Course obtenerClaseporId(Classroom servicio , String id) throws IOException {
        try {
            return servicio.courses().get(id).execute();
        }
        catch (Exception e){
            LOGGER.log(Level.WARNING, "La clase de ID" + id + "no pudo ser encontrada");
            return new Course();
        }
    }

    //Dar el nombre exacto de la clase
    public static Course obtenerClaseporNombre(Classroom servicio , String nombre) throws IOException {
        List<Course> clases = listarClases(servicio, totaldeClases(servicio));
        for (Course clase : clases) {
            if(Objects.equals(nombre ,clase.getName() ) ) return clase;
        }
        LOGGER.log(Level.WARNING, "La clase de nombre" + nombre + "no pudo ser encontrada");
        return new Course();
    }
    public static void archivarClase(Classroom servicio, String idClase ) throws IOException {
        try {
            Course clase = obtenerClaseporId(servicio, idClase);
            if (Objects.equals(clase.getCourseState(), "ARCHIVED")) {
                LOGGER.log(Level.WARNING, "La clase ya se encuentra archivada");
                return;
            }
            clase.setCourseState("ARCHIVED");
            servicio.courses().update(idClase, clase).execute();
        }
        catch (Exception e){
            LOGGER.log(Level.WARNING, "Hubo un problema con el servicio");
        }
    }

    // Se elimina una clase(antes de eliminar debe estar archivada)
    public static void eliminarClase(Classroom servicio , String id) throws IOException {
        try{
            servicio.courses().delete(id).execute();
            LOGGER.log(Level.INFO,java.text.MessageFormat.format( "La clase: {0} fue eliminada" , id ));
        }
        catch (Exception e){
            LOGGER.log(Level.INFO,java.text.MessageFormat.format( " No se pudo eliminar la clase de id {0}" , id ));
        }
    }


    //Se crea un nuevo topico, y se le agrega a una clase usando el id de la clase y el nombre del nuevo topico
    public static Topic agregarTopicoaClase(Classroom servicio , String idClase ,String nombreTopico ) throws IOException{
        Topic topico = new Topic();
        try{
            topico.set("name", nombreTopico);

            topico = servicio.courses().topics().create(idClase,topico).execute();
            LOGGER.log(Level.INFO,java.text.MessageFormat.format( "Se Creo el topico con id {0} para la clase con id {1}" , topico.getTopicId(), idClase ));
        }
        catch (Exception e){
            LOGGER.log(Level.WARNING , "No se pudo agregar el topico " + nombreTopico + " en la clase de id" + " " + idClase);
        }

        return topico;
    }

    public static int totaldeTopicosdeClase(Classroom servicio, String idClase) throws IOException{
        return servicio.courses().topics().list(idClase).size();
    }

    public static List<Topic> obtenerTopicosdeClase(Classroom servicio, String idClase , Integer cantidadTopicos ) throws IOException {

        try {
            ListTopicResponse response = servicio.courses().topics().list(idClase).setPageSize(cantidadTopicos).execute();

            if(response.isEmpty()) {
                LOGGER.log(Level.WARNING, "No hay topicos en la clase de id " + idClase);
                return new ArrayList<>();
            }
            return response.getTopic();
        }
        catch (Exception e){
            LOGGER.log(Level.WARNING, "Hubo un error con el servicio");
            return new ArrayList<>();
        }

    }

    //Se obtiene un topico dado el id de la clase a la que pertenece y su propio id
    public static Topic obtenerTopicoconIds(Classroom servicio, String idClase, String idTopico) throws IOException {
        return servicio.courses().topics().get(idClase,idTopico).execute();
    }

    //Dar el nombre exacto del topico y el id de la clase
    private static Topic obtenerTopicoporNombre(Classroom servicio, String idClase, String nombre) throws IOException {
        List<Topic> topicos = obtenerTopicosdeClase(servicio,idClase, totaldeTopicosdeClase(servicio,idClase));
        for (Topic topico : topicos) {
            if(Objects.equals(nombre ,topico.getName() ) ) return topico;
        }
        return new Topic() ;
    }

    // Se elimina un topico perteneciente a una clase
    public static void eliminarTopico(Classroom servicio, String idClase, String idTopico) throws IOException {
        try{
            servicio.courses().topics().delete(idClase,idTopico).execute();
            LOGGER.log(Level.INFO,java.text.MessageFormat.format( "El topico de id {0} de la clase con id: {1} fue eliminado" , idClase, idTopico ));
        }
        catch (Exception e){
            LOGGER.log(Level.INFO,java.text.MessageFormat.format( " No se pudo eliminar el topico de id {0}" , idTopico ));
        }

    }

    //Se crea una nueva tarea para algun topico perteneciente a una clase.
    public static CourseWork agregarTareaaTopico(Classroom servicio , String idClase , String idTopico, String tituloTarea ,String tipoTarea ) throws IOException{
        CourseWork tarea = new CourseWork();
        try {
            tarea.set("title", tituloTarea);
            tarea.set("workType", tipoTarea); // EL tipo de tarea puede ser: ASSIGNMENT,SHORT_ANSWER_QUESTION o MULTIPLE_CHOICE_QUESTION
            tarea.set("topicId", idTopico);
            tarea.set("state", "PUBLISHED"); // EL estado de la tarea puede ser: DRAFT o PUBLISHED
            tarea.setMaxPoints(20.0);

            tarea = servicio.courses().courseWork().create(idClase, tarea).execute();
            LOGGER.log(Level.INFO,java.text.MessageFormat.format("Se Creo la tarea con id {0} para el topico con id {1}", tarea.getId(), tarea.getTopicId()));
        }
        catch (Exception e){
            LOGGER.log(Level.WARNING , "No se pudo agregar la tarea de "+ tituloTarea +" al topico de id " + idTopico );
        }
        return tarea;
    }
    /*
    public static CourseWork agregarMaterial(Classroom servicio,String idClase, String idTarea) throws IOException{
        CourseWork tarea = obtenerTareaporClaseeId(servicio,idClase,idTarea);
    }
    */

    //Se obtiene una tarea dado el id de la clase a la que pertenece y su propio id
    public static CourseWork obtenerTareaporClaseeId(Classroom servicio, String idClase, String idTarea) throws IOException {
        return servicio.courses().courseWork().get(idClase,idTarea).execute();
    }

    //Se elimina un topico dado el id de la clase a la que pertenece y su propio id
    public static void eliminarTareadeClase(Classroom servicio, String idClase, String idTarea) throws IOException {

        try{
            servicio.courses().courseWork().delete(idClase,idTarea);
            LOGGER.log(Level.INFO, java.text.MessageFormat.format( "La tarea de id {0} de la clase con id: {1} fue eliminada" , idClase, idTarea ) );
        }
        catch (Exception e){
            LOGGER.log(Level.INFO,java.text.MessageFormat.format( " No se pudo eliminar la tarea de id {0}" , idTarea ));
        }



    }

    public static List<CourseWork> obtenerTareasdeClase(Classroom servicio, String idClase , Integer cantidadTareas ) throws IOException {
        try {
            ListCourseWorkResponse response = servicio.courses().courseWork().list(idClase).setPageSize(cantidadTareas).execute();

            if(response.isEmpty()) {
                LOGGER.log(Level.WARNING, "No hay tareas en la clase de id " + idClase);
                return new ArrayList<>();
            }
            return response.getCourseWork();
        }
        catch (Exception e){
            LOGGER.log(Level.WARNING, "Hubo un error con el servicio");
            return new ArrayList<>();
        }

    }

    public static int totaldeTareasdeClase(Classroom servicio, String idClase ) throws IOException {
        return servicio.courses().courseWork().list(idClase).size();
    }

    //Dar el nombre exacto de la tarea el id de la clase y el id del topico
    private static CourseWork obtenerTareasporNombre(Classroom servicio, String idClase, String idTopico, String nombre) throws IOException {
        List<CourseWork> tareas = obtenerTareasdeClase(servicio,idClase, totaldeTareasdeClase(servicio,idClase));
        for (CourseWork tarea : tareas) {
            if( (Objects.equals(tarea.getTopicId(), idTopico)) && Objects.equals(nombre ,tarea.getTitle() )  ) return tarea;
        }
        return new CourseWork();
    }

    //Se agregar un profesor, solo con su correo a la clase indicada, mediante su ID
    public static Teacher agregarProfesoraClase(Classroom servicio, String emailProfesor, String idClase) throws IOException{
        Teacher profesor = new Teacher();
        profesor.setUserId(emailProfesor);

        profesor  = servicio.courses().teachers().create(idClase,profesor).execute();

        System.out.println(java.text.MessageFormat.format( "Se agrego al profesor con id {0} a la clase con id {1}" , profesor.getUserId(), profesor.getCourseId() ));

        return profesor;
    }

    public static Student agregarAlumnoaClase(Classroom servicio, String emailAlumno, String idClase, String codigoClase) throws IOException{
        Student estudiante = new Student();

        try {
            estudiante.setUserId(emailAlumno);
            estudiante = servicio.courses().students().create(idClase, estudiante).execute();
            LOGGER.log(Level.INFO,java.text.MessageFormat.format("EL estudiante {0} con correo {1} se agrego a la clase con id {2}", estudiante.getProfile().getName().getFullName(), emailAlumno, idClase));
        }
        catch (Exception e){
            LOGGER.log(Level.WARNING, "No se pudo crear al nuevo alumno");
        }
        return estudiante;
    }

    public static void eliminarAlumnodeClase(Classroom servicio, String emailAlumno , String idClase) throws IOException{
        try{
            servicio.courses().students().delete(idClase,emailAlumno).execute();
            LOGGER.log(Level.INFO, java.text.MessageFormat.format( "El alumno con id {0} fue eliminado de la clase con id {1}" , emailAlumno, idClase ));
        }
        catch (Exception e){
            LOGGER.log(Level.WARNING , "No se pudo eliminar al alumno de correo "+emailAlumno+" de la clase de id "+idClase );
        }
    }

    public static void eliminarAlumnosdeClase(Classroom servicio, List<String> emails , String idClase) throws IOException{
        for(String email : emails){
            eliminarAlumnodeClase(servicio,email,idClase);
        }
    }

    //Se invita a una persona a una clase de acuerdo a su rol(puede ser alumno o profesor)
    public static void invitarPersonaaClase(Classroom servicio, String email, String idClase, String tipo ) throws IOException{
        // el tipo debe ser o "STUDENT" o TEACHER"

        Invitation invitacion = new Invitation();
        invitacion.setUserId(email);
        invitacion.setRole(tipo);
        invitacion.setCourseId(idClase);

        invitacion  = servicio.invitations().create(invitacion).execute();

        System.out.println(java.text.MessageFormat.format( "Se invito a la persona con id {0} a la clase con id {1}" , invitacion.getUserId(), invitacion.getCourseId() ));

    }

    public static Invitation obtenerInvitacionporId(Classroom servicio , String idInvitacion) throws IOException{
        return servicio.invitations().get(idInvitacion).execute();
    }

    public static void eliminarInvitacionPersona(Classroom servicio, String idInvitacion ) throws IOException{
        servicio.invitations().delete(idInvitacion);
    }


    //Generar varias clases con varios topicos y tareas (listas con nombres)
    public static List<String> cargaMasiva(Classroom servicio, List<String> listaClases, List<String> listaTopicos , List<String> listaTareas) throws IOException {
        List<String> clasesCreadas = new ArrayList<>();
        for (String nombreClase : listaClases) {
            Course claseActual = crearClase(servicio , nombreClase);
            for (String nombreTopico : listaTopicos) {
                Topic nuevoTopico = agregarTopicoaClase(servicio,claseActual.getId(),nombreTopico);
                for(String nombreTarea : listaTareas){
                    agregarTareaaTopico(servicio,claseActual.getId(),nuevoTopico.getTopicId(),nombreTarea,"ASSIGNMENT");
                }
            }
            clasesCreadas.add(claseActual.getId());
        }
        return clasesCreadas;
    }

    //Eliminar varias clases con sus respectivos topicos y tareas (listas con ids de clases)
    public static void borradoMasivo(Classroom servicio, List<String> listaClases) throws IOException {
        for (String idClase : listaClases) {

            List<Topic> listaTopicos = obtenerTopicosdeClase(servicio, idClase , totaldeTopicosdeClase(servicio , idClase));
            List<CourseWork> listaTareas = obtenerTareasdeClase(servicio, idClase , totaldeTareasdeClase(servicio , idClase));

            for(CourseWork tarea : listaTareas){
                eliminarTareadeClase(servicio,idClase,tarea.getId());
            }

            for(Topic topico : listaTopicos) {
                eliminarTopico(servicio,idClase,topico.getTopicId());
            }

            archivarClase(servicio, idClase);
            eliminarClase(servicio,idClase);
        }

    }

    private static List<Student> listaAlumnosClase(Classroom servicio, String idClase) throws IOException {
        Course clase = obtenerClaseporId(servicio, idClase);
        List<Student> estudiantes = new ArrayList<>();
        if(Objects.equals(clase.getCourseState(),"ACTIVE")) {
            ListStudentsResponse response = servicio.courses().students().list(clase.getId()).execute();
            List<Student> lista = response.getStudents();
            if(Objects.equals(lista,null)) return estudiantes;
            while(!Objects.equals(response.getNextPageToken(),null)){
                lista = response.getStudents();
                estudiantes.addAll(lista);
                response = servicio.courses().students().list(clase.getId()).setPageToken(response.getNextPageToken()).execute();
            }
            estudiantes.addAll(lista);
        }
        return estudiantes;
    }

    private static int totalAlumnosClase(Classroom servicio, String idClase) throws IOException {
        return listaAlumnosClase(servicio,idClase).size();
    }

    public static Student buscarEstudiante(Classroom servicio , String idClase , String emailAlumno ) throws IOException {
        try{
            Student estudiante = servicio.courses().students().get(idClase,emailAlumno).execute() ;
            if(Objects.equals(estudiante.getUserId(),null)) {
                LOGGER.log(Level.WARNING, "No se encontro al estudiante de correo "+ emailAlumno + "en la clase de id "+ idClase);
                return new Student();
            }
            return estudiante;
        }
        catch (Exception e){
            LOGGER.log(Level.WARNING, "Hubo un problema con el servicio");
            return new Student();
        }
    }

    public static Classroom obtenerServicio() throws IOException,GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new Classroom.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT)).setApplicationName(APPLICATION_NAME).build();
    }

}