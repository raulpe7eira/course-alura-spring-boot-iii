package br.com.alura.forum.controller;

import br.com.alura.forum.controller.dto.DetalheTopicoDto;
import br.com.alura.forum.controller.dto.TopicoDto;
import br.com.alura.forum.controller.form.AtualizaTopicoForm;
import br.com.alura.forum.controller.form.TopicoForm;
import br.com.alura.forum.model.Topico;
import br.com.alura.forum.repository.CursoRepository;
import br.com.alura.forum.repository.TopicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/topicos")
public class TopicosController {

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private TopicoRepository topicoRepository;

    @GetMapping
    @Cacheable(value = "listaTopicos")
    public Page<TopicoDto> lista(
            @RequestParam(required = false) String nomeCurso,
            @PageableDefault(sort = "id", direction = Direction.DESC) Pageable paginacao
    ) {
        Page<Topico> topicos = nomeCurso == null
                ? topicoRepository.findAll(paginacao)
                : topicoRepository.findByCursoNome(nomeCurso, paginacao);
        return TopicoDto.converter(topicos);
    }

    @PostMapping
    @Transactional
    @CacheEvict(value = "listaTopicos", allEntries = true)
    public ResponseEntity<TopicoDto> cadastrar(
            @RequestBody @Valid TopicoForm form,
            UriComponentsBuilder uriBuilder
    ) {
        Topico topico = form.converter(cursoRepository);
        topicoRepository.save(topico);

        URI uri = uriBuilder.path("/topicos/{id}").buildAndExpand(topico.getId()).toUri();
        return ResponseEntity.created(uri).body(new TopicoDto(topico));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetalheTopicoDto> detalhar(@PathVariable Long id) {
        Optional<Topico> optionalTopico = topicoRepository.findById(id);
        if (!optionalTopico.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new DetalheTopicoDto(optionalTopico.get()));
    }

    @PutMapping("/{id}")
    @Transactional
    @CacheEvict(value = "listaTopicos", allEntries = true)
    public ResponseEntity<TopicoDto> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid AtualizaTopicoForm form
    ) {
        Optional<Topico> optionalTopico = topicoRepository.findById(id);
        if (!optionalTopico.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Topico topico = form.atualizar(optionalTopico.get());
        return ResponseEntity.ok(new TopicoDto(topico));
    }

    @DeleteMapping("/{id}")
    @Transactional
    @CacheEvict(value = "listaTopicos", allEntries = true)
    public ResponseEntity remover(@PathVariable Long id) {
        Optional<Topico> optionalTopico = topicoRepository.findById(id);
        if (!optionalTopico.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        topicoRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
