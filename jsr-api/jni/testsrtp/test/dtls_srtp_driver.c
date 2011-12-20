/*
 * dtls_asrtpa_driver.c
 *
 * test driver for DTLS-SRTP functions
 *
 * David McGrew
 * Cisco Systems, Inc.
 */
/*
 *
 * Copyright (c) 2001-2006 Cisco Systems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 *   Neither the name of the Cisco Systems, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

#include <stdio.h>    /* for printf()          */
#include "getopt_s.h" /* for local getopt()    */
#include "srtp_priv.h"

err_status_t test_dtls_srtp();

static asrtpa_hdr_t *
asrtpa_create_test_packet(int pkt_octet_len, uint32_t ssrc);

// int main(int argc, char *argv[]) {
int dtls_asrtpa_driver(void) {
    unsigned do_list_mods = 0;
    char q;
    err_status_t err;

    printf("dtls_asrtpa_driver\n");

    /* initialize srtp library */
    err = asrtpa_init(1);
    if (err) {
        printf("error: srtp init failed with error code %d\n", err);
        return(1);
    }

    if (do_list_mods) {
        err = crypto_kernel_list_debug_modules();
        if (err) {
            printf("error: list of debug modules failed\n");
            return(1);
        }
    }

    printf("testing dtls_srtp...");
    err = test_dtls_srtp();
    if (err) {
        printf("\nerror (code %d)\n", err);
        return(1);
    }
    printf("passed\n");

    return 0;
}

err_status_t test_dtls_srtp() {
    asrtpa_hdr_t *test_packet;
    int test_packet_len = 80;
    asrtpa_t s;
    asrtpa_policy_t policy;
    uint8_t key[ASRTPA_MAX_KEY_LEN];
    uint8_t salt[ASRTPA_MAX_KEY_LEN];
    unsigned int key_len, salt_len;
    asrtpa_profile_t profile;
    err_status_t err;

    /* create a 'null' SRTP session */
    err = asrtpa_create(&s, NULL);
    if (err)
        return err;

    /*
     * verify that packet-processing functions behave properly - we
     * expect that these functions will return err_status_no_ctx
     */
    test_packet = asrtpa_create_test_packet(80, 0xa5a5a5a5);
    if (test_packet == NULL)
        return err_status_alloc_fail;
    err = asrtpa_protect(s, test_packet, &test_packet_len);
    if (err != err_status_no_ctx) {
        printf("wrong return value from asrtpa_protect() (got code %d)\n", err);
        return err_status_fail;
    }
    err = asrtpa_unprotect(s, test_packet, &test_packet_len);
    if (err != err_status_no_ctx) {
        printf("wrong return value from asrtpa_unprotect() (got code %d)\n", err);
        return err_status_fail;
    }
    err = asrtpa_protect_rtcp(s, test_packet, &test_packet_len);
    if (err != err_status_no_ctx) {
        printf("wrong return value from asrtpa_protect_rtcp() (got code %d)\n",
                err);
        return err_status_fail;
    }
    err = asrtpa_unprotect_rtcp(s, test_packet, &test_packet_len);
    if (err != err_status_no_ctx) {
        printf("wrong return value from asrtpa_unprotect_rtcp() (got code %d)\n",
                err);
        return err_status_fail;
    }

    /*
     * set keys to known values for testing
     */
    profile = asrtpa_profile_aes128_cm_sha1_80;
    key_len = asrtpa_profile_get_master_key_length(profile);
    salt_len = asrtpa_profile_get_master_salt_length(profile);
    memset(key, 0xff, key_len);
    memset(salt, 0xee, salt_len);
    append_salt_to_key(key, key_len, salt, salt_len);
    policy.key = key;

    /* initialize SRTP policy from profile  */
    err = crypto_policy_set_from_profile_for_rtp(&policy.rtp, profile);
    if (err)
        return err;
    err = crypto_policy_set_from_profile_for_rtcp(&policy.rtcp, profile);
    if (err)
        return err;
    policy.ssrc.type = ssrc_any_inbound;
    policy.next = NULL;

    err = asrtpa_add_stream(s, &policy);
    if (err)
        return err;

    return err_status_ok;
}

/*
 * asrtpa_create_test_packet(len, ssrc) returns a pointer to a
 * (malloced) example RTP packet whose data field has the length given
 * by pkt_octet_len and the SSRC value ssrc.  The total length of the
 * packet is twelve octets longer, since the header is at the
 * beginning.  There is room at the end of the packet for a trailer,
 * and the four octets following the packet are filled with 0xff
 * values to enable testing for overwrites.
 *
 * note that the location of the test packet can (and should) be
 * deallocated with the free() call once it is no longer needed.
 */

static asrtpa_hdr_t *
asrtpa_create_test_packet(int pkt_octet_len, uint32_t ssrc) {
    int i;
    uint8_t *buffer;
    asrtpa_hdr_t *hdr;
    int bytes_in_hdr = 12;

    /* allocate memory for test packet */
    hdr = malloc(pkt_octet_len + bytes_in_hdr + ASRTPA_MAX_TRAILER_LEN + 4);
    if (!hdr)
        return NULL;

    hdr->version = 2; /* RTP version two     */
    hdr->p = 0; /* no padding needed   */
    hdr->x = 0; /* no header extension */
    hdr->cc = 0; /* no CSRCs            */
    hdr->m = 0; /* marker bit          */
    hdr->pt = 0xf; /* payload type        */
    hdr->seq = htons(0x1234); /* sequence number     */
    hdr->ts = htonl(0xdecafbad); /* timestamp           */
    hdr->ssrc = htonl(ssrc); /* synch. source       */

    buffer = (uint8_t *) hdr;
    buffer += bytes_in_hdr;

    /* set RTP data to 0xab */
    for (i = 0; i < pkt_octet_len; i++)
        *buffer++ = 0xab;

    /* set post-data value to 0xffff to enable overrun checking */
    for (i = 0; i < ASRTPA_MAX_TRAILER_LEN + 4; i++)
        *buffer++ = 0xff;

    return hdr;
}
