package com.wiilink24.bot.commands.misc;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageSource;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Command that pulls text from images.
 * The main use is for WiiLink Translators.
 *
 * @author Sketch
 */

public class OCR {
    public OCR() {}

    public void ocr(SlashCommandEvent event) {
        try {
            event.deferReply().queue();
            List<AnnotateImageRequest> requests = new ArrayList<>();
            Image img;

            ImageSource imgSource = ImageSource.newBuilder().setImageUri(event.getOptionsByName("uri").get(0).getAsString()).build();
            img = Image.newBuilder().setSource(imgSource).build();


            Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
            AnnotateImageRequest request =
                    AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
            requests.add(request);

            try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
                BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
                List<AnnotateImageResponse> responses = response.getResponsesList();

                for (AnnotateImageResponse res : responses) {
                    if (res.hasError()) {
                        System.out.format("Error: %s%n", res.getError().getMessage());
                        event.getHook().sendMessage("Error: " + res.getError().getMessage()).queue();
                        return;
                    }

                    // The text we need will always be in the first index.
                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle("Optical Character Recognition")
                            .setColor(0xADD8E6)
                            .setDescription(String.format("```%s```",res.getTextAnnotationsList().get(0).getDescription()));

                    client.close();
                    event.getHook().sendMessageEmbeds(embed.build()).queue();
                }
            }
        } catch (IOException e) {
            event.getHook().sendMessage("An error has occurred: " + e.getMessage()).setEphemeral(true).queue();
            Sentry.captureException(e);
        }
    }
}
