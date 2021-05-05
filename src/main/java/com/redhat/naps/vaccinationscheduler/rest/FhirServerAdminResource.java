package com.redhat.naps.vaccinationscheduler.rest;

import com.redhat.naps.vaccinationscheduler.FhirServerAdminService;
import com.redhat.naps.vaccinationscheduler.domain.FhirServerAdminConfig;
import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ca.uhn.fhir.context.FhirContext;

@Path("/fhirServerAdmin")
public class FhirServerAdminResource {

    private static FhirContext fhirCtx = FhirContext.forR4();

    @Inject
    FhirServerAdminService fsAdminService;
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/seedFhirServer/")
    public Response seedFhirServer(final FhirServerAdminConfig adminConfig) throws InterruptedException, IOException {

        int patientGeneratorCount = fsAdminService.seedFhirServer(adminConfig);
        return Response.ok(patientGeneratorCount).build();
    }

}
