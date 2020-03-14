import com.google.api.services.classroom.Classroom;
import com.google.api.services.admin.directory.Directory;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class Main {

    public static void main(String... args) throws IOException, GeneralSecurityException {
        ClassroomJavaAPI classroomAPI = new ClassroomJavaAPI();
        GsuiteJavaAPI gsuiteAPI = new GsuiteJavaAPI();

        Classroom servicioClassroom = classroomAPI.obtenerServicio();
        Directory servicioUsuario = gsuiteAPI.obtenerServicio();
        //TODO: Hacer demo con las clases

    }
}
