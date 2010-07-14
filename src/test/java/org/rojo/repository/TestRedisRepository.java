package org.rojo.repository;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.jredis.RedisException;
import org.jredis.ri.alphazero.JRedisClient;
import org.jredis.ri.alphazero.support.DefaultCodec;
import org.junit.Before;
import org.junit.Test;

import org.rojo.domain.Person;
import org.rojo.exceptions.InvalidTypeException;
import org.rojo.repository.RedisRepository;
import org.rojo.repository.TypeConverter;
import org.rojo.repository.converters.Converters;
import org.rojo.repository.converters.IntegerConverter;
import org.rojo.repository.converters.StringConverter;

public class TestRedisRepository {

    private RedisRepository target;
    private JRedisClient jrClient;
    
    @Before
    public void init() throws RedisException  {
        jrClient= mock(JRedisClient.class);
        target = new RedisRepository(jrClient, initConverters());
    }
    
    private Converters initConverters() {
        List<TypeConverter> converters = new ArrayList<TypeConverter>(2);
        converters.add(new IntegerConverter());
        converters.add(new StringConverter());
        return new Converters(converters);
    }

    @Test(expected = InvalidTypeException.class)
    public void invalidEntityRequest() {
        target.get(new FooTestClass(),  2);
    }
    
    @Test
    public void validEntityRequest() throws RedisException {
        
        when(jrClient.get("org.rojo.domain.person:2:name")).thenReturn(new String("mikael foobar").getBytes());
        when(jrClient.get("org.rojo.domain.person:2:age")).thenReturn(DefaultCodec.<Integer>encode(33));
        when(jrClient.get("org.rojo.domain.person:2:address")).thenReturn(new Long(6).toString().getBytes());
      
        when(jrClient.get("org.rojo.domain.address:6:town")).thenReturn(new String("Stockholm").getBytes());
        when(jrClient.get("org.rojo.domain.address:6:street")).thenReturn(new String("Lundagatan").getBytes());
        
        Person person = target.get(new Person(), 2);

        assertEquals("mikael foobar", person.getName());
        assertEquals(33, person.getAge());
        assertEquals(2, person.getId());
        
        assertEquals("Stockholm", person.getAddress().getTown());
        assertEquals("Lundagatan", person.getAddress().getStreet());
        assertEquals(6, person.getAddress().getId());
        
    }
    
    public class FooTestClass {
    }
    
}