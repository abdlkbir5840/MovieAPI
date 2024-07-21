package com.movieflex.services;

import com.movieflex.dto.MovieDto;
import com.movieflex.dto.MoviePageResponse;
import com.movieflex.entities.Movie;
import com.movieflex.exceptions.FileExcistsException;
import com.movieflex.exceptions.MovieNoteFoundException;
import com.movieflex.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovieServiceImpl implements MovieService {
    private final MovieRepository movieRepository;

    private  final FileService fileService;

    @Value("${project.poster}")
    private  String path;

    @Value("${base.url}")
    private  String baseUrl;

    public MovieServiceImpl(MovieRepository movieRepository, FileService fileService) {
        this.movieRepository = movieRepository;
        this.fileService = fileService;
    }

    @Override
    public MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws IOException {
        // upload the file
        if(Files.exists(Paths.get(path + File.separator + file.getOriginalFilename()))) {
            throw new FileExcistsException("File already exists, please choose a different filename");
        }
        String uploadedFileName = fileService.uploadFile(path, file);
        // set the value of field poster as filename
        movieDto.setPoster(uploadedFileName);
        // map dto to movei object
        Movie movie = new Movie(
                null,
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );
        // save the movie object --> saved movie object
        Movie savedMovie = movieRepository.save(movie);
        // generate the posterUrl
        String posterUrl = baseUrl + "/file/" + uploadedFileName;
        // map movie object to dto object and return it
        MovieDto response = new MovieDto(
                savedMovie.getMovieId(),
                savedMovie.getTitle(),
                savedMovie.getDirector(),
                savedMovie.getStudio(),
                savedMovie.getMovieCast(),
                savedMovie.getReleaseYear(),
                savedMovie.getPoster(),
                posterUrl
        );
        return response;
    }

    @Override
    public MovieDto getMovieById(Integer movieId) {
        // check the data in db and if exists, fetch the data of given id
        Movie existingMovie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNoteFoundException("Movie not found with id : "+ movieId));
        // generate posterurk
        String posterUrl = baseUrl + "/file/" + existingMovie.getPoster();
        // map to movieDto object and return it
        MovieDto response = new MovieDto(
                existingMovie.getMovieId(),
                existingMovie.getTitle(),
                existingMovie.getDirector(),
                existingMovie.getStudio(),
                existingMovie.getMovieCast(),
                existingMovie.getReleaseYear(),
                existingMovie.getPoster(),
                posterUrl
        );
        return response;
    }

    @Override
    public List<MovieDto> getAllMovies() {
        // fetch all data from db
        List<Movie> movies = movieRepository.findAll();
        // iterate throught the list, generate posterUrl for each movie object
        // and map to MovieDto obj
        List<MovieDto> movieDtos = new ArrayList<>();
        for (Movie movie : movies) {
            String posterUrl = baseUrl + "/file/" + movie.getPoster();
            MovieDto movieDto = new MovieDto(
                    movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl
            );
            movieDtos.add(movieDto);
        }
        return movieDtos;
    }

    @Override
    public MovieDto updateMovie(Integer movieId, MovieDto movieDto, MultipartFile file) throws IOException {
        // check if movie object exists with given id
        Movie existingMovie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNoteFoundException("Movie not found with id : "+ movieId));
        // if file is null, do nothing
        // if file is not null, then delete existing file associated to record
        //and upload the new file
        String fileName = existingMovie.getPoster();
        if(file !=null){
            Files.delete(Paths.get(path + File.separator + fileName));
            fileName = fileService.uploadFile(path, file);
        }
        /// set movieDto's poster value, according to steps 2
        movieDto.setPoster(fileName);

        // map it to Movie object
        Movie movie = new Movie(
                existingMovie.getMovieId(),
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );
        // save the movie object -~ return saved movie object
        Movie savedMovie = movieRepository.save(movie);
        // generate posterUrl for it
        String posterUrl = baseUrl + "/file/" + savedMovie.getPoster();
        // map to MovieDto and return it
        MovieDto response = new MovieDto(
                savedMovie.getMovieId(),
                savedMovie.getTitle(),
                savedMovie.getDirector(),
                savedMovie.getStudio(),
                savedMovie.getMovieCast(),
                savedMovie.getReleaseYear(),
                savedMovie.getPoster(),
                posterUrl
        );
        return response;
    }

    @Override
    public void deleteMovie(Integer movieId) throws IOException {
        // check if movie exist with given id
        Movie existingMovie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNoteFoundException("Movie not found with id : "+ movieId));
        // delete the file associatd with this object
        Files.deleteIfExists(Paths.get(path+ File.separator + existingMovie.getPoster()));
        // delete the movie object
        movieRepository.deleteById(movieId);
    }

    @Override
    public MoviePageResponse getAllMoviesWithPagination(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Movie> moviePage = movieRepository.findAll(pageable);
        List<Movie> movies = moviePage.getContent();

        List<MovieDto> movieDtos = new ArrayList<>();
        for (Movie movie : movies) {
            String posterUrl = baseUrl + "/file/" + movie.getPoster();
            MovieDto movieDto = new MovieDto(
                    movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl
            );
            movieDtos.add(movieDto);
        }
        return new MoviePageResponse(movieDtos, pageNumber, pageSize,
                moviePage.getTotalElements(),
                moviePage.getTotalPages(),
                moviePage.isLast()
        );
    }

    @Override
    public MoviePageResponse getAllMoviesWithPaginationAndSorting(Integer pageNumber, Integer pageSize,
                                                                  String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Movie> moviePage = movieRepository.findAll(pageable);
        List<Movie> movies = moviePage.getContent();

        List<MovieDto> movieDtos = new ArrayList<>();
        for (Movie movie : movies) {
            String posterUrl = baseUrl + "/file/" + movie.getPoster();
            MovieDto movieDto = new MovieDto(
                    movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl
            );
            movieDtos.add(movieDto);
        }
        return new MoviePageResponse(movieDtos, pageNumber, pageSize,
                moviePage.getTotalElements(),
                moviePage.getTotalPages(),
                moviePage.isLast()
        );
    }
}
