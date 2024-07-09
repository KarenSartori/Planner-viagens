package com.rocketseat.planner.trip;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rocketseat.planner.participant.Participant;
import com.rocketseat.planner.participant.ParticipantCreateResponse;
import com.rocketseat.planner.participant.ParticipantData;
import com.rocketseat.planner.participant.ParticipantRequestPayload;
import com.rocketseat.planner.participant.ParticipantService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;


@RestController
@RequestMapping("/trips")  //nome da tabela
public class TripController {


    @Autowired
    private ParticipantService participantService;

    @Autowired
    private TripRepository repository;

    //metodo para enviar os dados da viagem
    @PostMapping 
    public ResponseEntity<TripCreateResponse> createTrip (@RequestBody TripRequestPayload payload ) {
        
        Trip newTrip = new Trip(payload);

        this.repository.save(newTrip);

        this.participantService.registerParticipantsToEvent(payload.emails_to_invite(), newTrip);

        return ResponseEntity.ok(new TripCreateResponse(newTrip.getId()));

    }

    //metodo para recuperar os dados da viagem
    @GetMapping("/{id}") 
    public ResponseEntity<Trip> getTripDetails(@PathVariable UUID id){
        Optional<Trip> trip = this.repository.findById(id);

        return trip.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    //metodo para atualizar informações da viagem
    @PutMapping("/{id}") 
    public ResponseEntity<Trip> updateTrip(@PathVariable UUID id, @RequestBody TripRequestPayload payload){ //o TripRequestPayload é as informações da viagem
        Optional<Trip> trip = this.repository.findById(id);

        if(trip.isPresent()) { //confirma se a trip existiu e atualiza as informações
            Trip rawTrip = trip.get();
            rawTrip.setEndsAt(LocalDateTime.parse(payload.ends_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setStartsAt(LocalDateTime.parse(payload.starts_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setDestination(payload.destination());

            this.repository.save(rawTrip);

            return ResponseEntity.ok(rawTrip);
        }

        return ResponseEntity.notFound().build(); //caso nao exista a viagem 
    }

    
    //metodo de confirmação da viagem
    @GetMapping("/{id}/confirm") 
    public ResponseEntity<Trip> confirmTrip(@PathVariable UUID id){ 
        Optional<Trip> trip = this.repository.findById(id);  //procurar no banco de dados a viagem cm o ID

        if(trip.isPresent()) { //confirma se a trip existe
            Trip rawTrip = trip.get();
            rawTrip.setIsConfirmed(true);

            this.repository.save(rawTrip); //efetivar a mudança no banco de dados
            this.participantService.triggerConfirmationEmailToParticipants(id); //participante responsavel manda os emails 

            return ResponseEntity.ok(rawTrip);
        }

        return ResponseEntity.notFound().build(); //caso nao exista a viagem 
    }


    //metodo para convidar participante pra viagem
    @PostMapping("/{id}/invite")
    public ResponseEntity<ParticipantCreateResponse> inviteParticipant (@PathVariable UUID id, @RequestBody ParticipantRequestPayload payload ) {
        Optional<Trip> trip = this.repository.findById(id);  //procurar no banco de dados a viagem cm o ID

        if(trip.isPresent()) { //confirma se a trip existe
            Trip rawTrip = trip.get();


            ParticipantCreateResponse participantResponse = this.participantService.registerParticipantToEvent(payload.email(), rawTrip); //participante responsavel manda os emails 

            if(rawTrip.getIsConfirmed()) this.participantService.triggerConfirmationEmailToParticipant(payload.email());

            return ResponseEntity.ok(participantResponse);
        }

        return ResponseEntity.notFound().build(); //caso nao exista a viagem 

    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ParticipantData>> getAllParticipants(@PathVariable UUID id){
        List<ParticipantData> participantList = this.participantService.getAllParticipantsFromEvent(id);

        return ResponseEntity.ok(participantList);
    }

}
