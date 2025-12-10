package org.linhtk.orchestrator.config.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;

/**
 * Custom Hibernate UserType for handling PostgreSQL vector type.
 * Converts between Java float[] arrays and PostgreSQL vector columns.
 * 
 * PostgreSQL vector format: '[x1,x2,x3,...]'
 * This type ensures proper serialization/deserialization for pgvector extension.
 */
public class VectorType implements UserType<float[]> {

    @Override
    public int getSqlType() {
        // Use OTHER type for custom PostgreSQL types
        return Types.OTHER;
    }

    @Override
    public Class<float[]> returnedClass() {
        return float[].class;
    }

    @Override
    public boolean equals(float[] x, float[] y) {
        return Arrays.equals(x, y);
    }

    @Override
    public int hashCode(float[] x) {
        return Arrays.hashCode(x);
    }

    @Override
    public float[] nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner)
            throws SQLException {
        String vectorString = rs.getString(position);
        if (vectorString == null) {
            return null;
        }
        return parseVector(vectorString);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, float[] value, int index, SharedSessionContractImplementor session)
            throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            // Convert float array to PostgreSQL vector format: '[x1,x2,x3,...]'
            String vectorString = formatVector(value);
            st.setObject(index, vectorString, Types.OTHER);
        }
    }

    @Override
    public float[] deepCopy(float[] value) {
        if (value == null) {
            return null;
        }
        return Arrays.copyOf(value, value.length);
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(float[] value) {
        return deepCopy(value);
    }

    @Override
    public float[] assemble(Serializable cached, Object owner) {
        return deepCopy((float[]) cached);
    }

    @Override
    public float[] replace(float[] detached, float[] managed, Object owner) {
        return deepCopy(detached);
    }

    /**
     * Converts a float array to PostgreSQL vector string format.
     * Format: '[x1,x2,x3,...]'
     * 
     * @param vector the float array to convert
     * @return PostgreSQL vector string representation
     */
    private String formatVector(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Parses a PostgreSQL vector string to float array.
     * Expected format: '[x1,x2,x3,...]'
     * 
     * @param vectorString the PostgreSQL vector string
     * @return parsed float array
     * @throws HibernateException if parsing fails
     */
    private float[] parseVector(String vectorString) {
        try {
            // Remove brackets and split by comma
            String content = vectorString.substring(1, vectorString.length() - 1);
            if (content.isEmpty()) {
                return new float[0];
            }
            
            String[] parts = content.split(",");
            float[] result = new float[parts.length];
            
            for (int i = 0; i < parts.length; i++) {
                result[i] = Float.parseFloat(parts[i].trim());
            }
            
            return result;
        } catch (Exception e) {
            throw new HibernateException("Failed to parse vector string: " + vectorString, e);
        }
    }
}
