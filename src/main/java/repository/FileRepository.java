package repository;

import domain.entities.Entity;
import domain.exceptions.DuplicateEntityException;
import domain.exceptions.RepositoryException;
import domain.exceptions.ValidationException;
import domain.validators.Validator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FileRepository<ID, E extends Entity<ID>> extends InMemoryRepository<ID, E>{
    private final Path path;
    private final Function<String, E> lineToEntity;
    private final Function<E, String> entityToLine;

    public FileRepository(Validator<E> validator, String fileName, Function<String, E> lineToEntity, Function<E,String> entityToLine, Supplier<ID> idGenerator){
        super(validator,idGenerator);
        this.path = Paths.get(fileName);
        this.lineToEntity = lineToEntity;
        this.entityToLine = entityToLine;
        loadFromFile();
    }

    private void loadFromFile() {
        if(!Files.exists(path)){
            return;
        }
        try (Stream<String> stream = Files.lines(path, StandardCharsets.UTF_8)) {
            List<String> lines = stream.collect(Collectors.toList());
            IntStream.range(0, lines.size())
                    .filter(i -> !lines.get(i).trim().isEmpty())
                    .forEach(i -> {
                        try {
                            saveWithoutWriting(lineToEntity.apply(lines.get(i)));
                        } catch (ValidationException | DuplicateEntityException | RepositoryException e) {
                            System.out.println("Ignored line " + (i + 1) + " of " + path + ": " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            throw new RepositoryException("Could not read file " + path, e);
        }
    }


    private void saveWithoutWriting(E e) {
        super.save(e);
    }

    private void writeToFile(){
        try{
            Path parent = path.getParent();
            if(parent != null){
                Files.createDirectories(parent);
            }
            List<String> lines = StreamSupport.stream(findAll().spliterator(), false)
                    .map(entityToLine)
                    .collect(Collectors.toList());
            Files.write(path,lines,StandardCharsets.UTF_8);
        }catch(IOException e){
            throw new RepositoryException("Could not write file " + path, e);
        }
    }

    @Override
    public Optional<E> update(E e) {
        Optional<E> result = super.update(e);
        if (!result.isPresent()) {
            writeToFile();
        }
        return result;
    }

    @Override
    public void save(E e){
        super.save(e);
        writeToFile();
    }

    @Override
    public Optional<E> delete(ID id){
        Optional<E> result=super.delete(id);
        if(result.isPresent()){
            writeToFile();
        }
        return result;
    }

}
