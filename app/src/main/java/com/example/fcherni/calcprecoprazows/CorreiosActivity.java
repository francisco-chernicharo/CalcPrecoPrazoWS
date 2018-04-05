package com.example.fcherni.calcprecoprazows;

import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class CorreiosActivity extends AppCompatActivity {

	EditText editTextCepOrigem, editTextCepDestino;
	Spinner spinnerServiceType;
	TextView textViewPrazo, textViewPrazoResult;
	TextView textViewEntrSab, textViewEntrSabResult;
	Button buttonCalcular;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_correios);

		/**Autorizando a utilização do WebService*/
		if (Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		editTextCepOrigem = (EditText) findViewById(R.id.editTextCEPOrigem);
		editTextCepDestino = (EditText) findViewById(R.id.editTextCEPDestino);

		spinnerServiceType = (Spinner) findViewById(R.id.spinnerServiceTypes);
		ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(this, R.array.arrServiceTypes,
				R.layout.support_simple_spinner_dropdown_item);
		arrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
		spinnerServiceType.setAdapter(arrayAdapter);


		textViewPrazo = (TextView) findViewById(R.id.textViewPrazo);
		textViewPrazoResult = (TextView) findViewById(R.id.textViewPrazoResult);

		textViewEntrSab = (TextView) findViewById(R.id.textViewEntrSab);
		textViewEntrSabResult = (TextView) findViewById(R.id.textViewEntrSabResult);

		buttonCalcular = (Button) findViewById(R.id.buttonCalcular);

	}


	public void calcularPrazo(View v) {

		if(!( (editTextCepDestino.getText().toString().equals(""))
				|| editTextCepOrigem.getText().toString().equals("")
				)){

		//Informações da Conexão WebServices
		final String NAMESPACE = "http://tempuri.org/";
		final String METHOD_NAME = "CalcPrazo";
		final String SOAP_ACTION = "http://tempuri.org/CalcPrazo";
		final String URLCorreios =
				"http://ws.correios.com.br/calculador/CalcPrecoPrazo.asmx?wsdl";
		/*A URL acima leva ao código do WebService, em XML, que é o padrão de comunicação*
		do SOAP
		 */

		/*Requisição SOAP - leva as informações a serem enviadas*/
		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);


		String ncdServico = "";

		switch (spinnerServiceType.getSelectedItem().toString()) {
			/*Cód de ncd de serviço é fornecido pelos correios*/
			case "SEDEX Varejo":
				ncdServico = "40010";
				break;
			case "SEDEX a Cobrar Varejo":
				ncdServico = "40045";
				break;
			case "SEDEX 10 Varejo":
				ncdServico = "40215";
				break;
			case "SEDEX Hoje Varejo":
				ncdServico = "40290";
				break;
			case "PAC Varejo":
				ncdServico = "41106";
				break;
		}


		//Adicionando informações à requisição
		request.addProperty("nCdServico", ncdServico);
		request.addProperty("sCepOrigem", editTextCepOrigem.getText().toString());
		request.addProperty("sCepDestino", editTextCepDestino.getText().toString());

//A requisição é enviada encapsulada em um objeto Envelope
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

//Os correios usam o padrão .NET -> o .asxm do URI quer dizer isso
		envelope.dotNet = true;

//Colocando a Request dentro do Envelope
		envelope.setOutputSoapObject(request);

//Colocando o Envelope no protocolo HTTP de transporte
		HttpTransportSE transport = new HttpTransportSE(URLCorreios);

		try {

//transport.call realiza a chamada e envia o Envelope
			transport.call(SOAP_ACTION, envelope);

			SoapObject response = (SoapObject) envelope.bodyIn;
/**anyType{Servicos=anyType{cServico=anyType{Codigo=40010; PrazoEntrega=0;
 *  EntregaDomiciliar=anyType{}; EntregaSabado=anyType{}; Erro=999; MsgErro=CEP inexistente*/
//
			SoapObject responseAnyType = (SoapObject) response.getProperty(0);
			SoapObject responseServicos = (SoapObject) responseAnyType.getProperty("Servicos");
			SoapObject responseCServico = (SoapObject) responseServicos.getProperty("cServico");


			textViewPrazoResult.setText((responseCServico.getProperty("PrazoEntrega")

					.toString() + "dias"));


			if (responseCServico
					.getProperty("EntregaSabado").toString().equals("S")) {
				textViewEntrSabResult.setText("Sim");
			} else
				textViewEntrSabResult.setText("Não");

			textViewPrazoResult.setVisibility(View.VISIBLE);
			textViewEntrSabResult.setVisibility(View.VISIBLE);


			Log.e("SOAP_Response", response.getProperty(0).toString());
			Log.e("SOAP_Response", responseAnyType.getProperty("Servicos").toString());

		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}}
