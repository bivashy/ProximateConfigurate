package com.bivashy.configurate;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.*;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import com.bivashy.configurate.objectmapping.ConfigInterface;
import com.bivashy.configurate.objectmapping.common.InterfaceObjectMapperFactory;
import com.bivashy.configurate.objectmapping.meta.Style;
import com.bivashy.configurate.objectmapping.meta.Transient;

public class ObjectContractTest {

    private final ConfigurationNode node = loadConfiguration();

    static ConfigurationNode loadConfiguration() {
        InputStream resourceStream = ObjectContractTest.class.getResourceAsStream("/simple-model.conf");
        assertNotNull(resourceStream);
        try {
            return HoconConfigurationLoader.builder()
                    .source(() -> new BufferedReader(new InputStreamReader(resourceStream)))
                    .defaultOptions(opt ->
                            opt.serializers(builder -> builder
                                    .registerAll(TypeSerializerCollection.defaults())
                                    .register(InterfaceObjectMapperFactory::applicable, new InterfaceObjectMapperFactory())
                            ))
                    .build()
                    .load();
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testToString() throws ConfigurateException {
        Student student = node.get(Student.class);
        StudentWithoutClass studentWithoutClass = node.get(StudentWithoutClass.class);
        assertNotNull(student, "student");
        assertNotNull(studentWithoutClass, "studentWithoutClass");
        String studentString = student.toString();
        String studentWithoutClassString = studentWithoutClass.toString();
        assertNotNull(studentString, "studentString");
        assertNotNull(studentWithoutClassString, "studentWithoutClassString");
        assertEquals("[interface com.bivashy.configurate.ObjectContractTest$Student]{schoolClass=Class{peopleAmount = 20, grade = 11}, name=Ben, age=17}",
                studentString);
        assertEquals("[interface com.bivashy.configurate.ObjectContractTest$StudentWithoutClass]{name=Ben, age=17}", studentWithoutClassString);
    }

    @Test
    void testGeneratedEqualsAndHashCodeContract() throws SerializationException {
        StudentWithoutClass student = node.get(StudentWithoutClass.class);
        StudentWithoutClass alexStudent = node.node("alex-student").get(StudentWithoutClass.class);
        StudentWithoutClass equalStudent = node.node("equal-student").get(StudentWithoutClass.class);
        StudentWithoutClass secondEqualStudent = node.node("second-equal-student").get(StudentWithoutClass.class);

        assertNotNull(student);
        assertNotNull(alexStudent);
        assertNotNull(equalStudent);
        assertNotNull(secondEqualStudent);

        assertNotEquals(null, student, "student.equals(null)");
        // reflexive
        assertEquals(student, student);
        assertEquals(student.hashCode(), student.hashCode());
        // symmetric
        assertEquals(student, equalStudent);
        assertEquals(equalStudent, student);
        assertEquals(student.hashCode(), equalStudent.hashCode());
        // transitive
        assertEquals(student, equalStudent);
        assertEquals(equalStudent, secondEqualStudent);
        assertEquals(student.hashCode(), secondEqualStudent.hashCode());
        assertEquals(equalStudent.hashCode(), secondEqualStudent.hashCode());

        assertNotEquals(alexStudent, student);
        assertNotEquals(alexStudent, equalStudent);
        assertNotEquals(alexStudent, secondEqualStudent);

        assertNotEquals(alexStudent.hashCode(), student.hashCode());
        assertNotEquals(alexStudent.hashCode(), equalStudent.hashCode());
        assertNotEquals(alexStudent.hashCode(), secondEqualStudent.hashCode());
    }

    @Test
    void testHashCodeWithHashMap() throws SerializationException {
        Map<StudentWithoutClass, String> map = new HashMap<>();
        StudentWithoutClass student = node.get(StudentWithoutClass.class);
        StudentWithoutClass otherStudent = node.node("alex-student").get(StudentWithoutClass.class);
        map.put(student, "First");

        assertEquals("First", map.get(student));
        assertNull(map.get(otherStudent));
        map.put(otherStudent, "Second");
        assertEquals("Second", map.get(otherStudent));
    }

    @Test
    void testStyledEqualsContract() throws SerializationException {
        SchoolClass schoolClass = node.node("school-class").get(SchoolClass.class);
        SchoolClass equalSchoolClass = node.node("other-school-class").get(SchoolClass.class);
        assertEquals(schoolClass, equalSchoolClass);
        assertEquals(schoolClass.hashCode(), equalSchoolClass.hashCode());
        assertNotEquals(schoolClass.toString(), equalSchoolClass.toString());
    }

    @ConfigInterface
    public interface StudentWithoutClass {

        String name();

        int age();

    }
    @ConfigInterface
    public interface Student extends StudentWithoutClass {

        SchoolClass schoolClass();

    }
    @ConfigInterface
    @Style(equalsName = "equal", toStringName = "string", hashCodeName = "customHashCode")
    public interface SchoolClass {

        int peopleAmount();

        int grade();

        @Transient
        default boolean equal(Object object) {
            if (object == this)
                return true;
            if (object == null || !object.getClass().equals(getClass()))
                return false;
            SchoolClass schoolClass = (SchoolClass) object;
            return Objects.equals(grade(), schoolClass.grade());
        }

        @Transient
        default String string() {
            return "Class{peopleAmount = " + peopleAmount() + ", grade = " + grade() + "}";
        }

        @Transient
        default int customHashCode() {
            return Objects.hash(grade());
        }

    }

}
