# TrackerLoL
# _*(Português)*_
Isso é um código criado com Java e HTML para ler cooldowns, as runas principais e a arvore secundária com interface para marcar feitiços do inimigo quando quiser/ This is code created to read cooldowns, all runes from both trees (primary and secondary)..

Nesse código tem alguns detalhes para explicar:

*Antes disso tudo tenha o Java JDK 17, 21+ instalado no computador para o código funcionar!*

1 - Os dados do tempo de recarga dos campeões vem desse site onde tem todos os dados estáticos aqui: https://ddragon.leagueoflegends.com/cdn/16.8.1/data/pt_BR/champion.json. O Data Dragon (DDragon) fornece dados estáticos do League of Legends, incluindo IDs de campeões (campo chave, numérico) e chaves (campo id, baseado em nome), acessíveis por meio de arquivos JSON.

***********************************************************************************************************************************************************************************************************************

2 - Porém os dados desse site, alguns tempo de recarga de campeões estão desatualizados, pois eles demoram mais pra atualizar esses dados do site do que o próprio jogo.

------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

3 - Então, e quando o Tracker rastrear um campeão, como vou saber que o tempo de recarga está errado? Você pode simplesmente ir no site u.gg ou op.gg e procurar pelo campeão e ver o tempo de recarga das habilidades, eles têm uma equipe gigante e um banco de dados muito mais estruturado para isso, pois eles atualizam manualmente a cada atualização e não dependem totalmente desse site Data DDragon.

------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

4 - Então para que serve o Tracker se ele pode me dar informações erradas sobre o tempo de recarga de algum campeão? Bom, no código disponibilizado que você mesmo pode editar nesta parte do código aqui: <img width="1332" height="882" alt="image" src="https://github.com/user-attachments/assets/90fa2f70-0438-4f26-b3d1-a87deb2b5e25" />

*Como isso funciona? Esse comando que está destacado, nós podemos mudar permanentemente os dados dos campeões que estão desatualizados para os novos dados do tempo de recarga dos campeões vistos no site "u.gg" ou "op.gg".*

*Como podem ver no comando que está destacado, já tem alguns comentários explicando como é feito, basta colocar adicionar o comando: "put("NomeDoCampeao", new String[]{"Q", "W", "E", "R"});"* 

*E substitua os valores como mostrado no exemplo do comando e por fim, dar um rebuild no código para ele carregar essa nova alteração. É quase tipo o que a equipe dos sites u.gg e op.gg faz.*

------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

OBS: Caso queira desativar o overlay dessas spells, basta ir no código e comentar essas partes aqui:

<img width="1228" height="566" alt="image" src="https://github.com/user-attachments/assets/3a0bc156-f6d7-4973-9292-eb6efac29c9c" />



------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

# _*(English)*_

This is code created with Java and HTML to read cooldowns, the primary runes, and the secondary tree, with an interface to mark enemy spells whenever you want.

This code has some details to explain:

*Before anything else, you need Java JDK 17, 21+ installed on your computer for the code to work!*

1 - The champion cooldown time data comes from this website, which has all the static data here: https://ddragon.leagueoflegends.com/cdn/16.8.1/data/pt_BR/champion.json. Data Dragon (DDragon) provides static League of Legends data, including champion IDs (key field, numeric) and keys (id field, name-based), accessible through JSON files.

***********************************************************************************************************************************************************************************************************************

2 - However, the data on this site, some champion cooldown times are outdated, because they take longer to update this data on the site than the game itself.

------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

3 - So, when the Tracker tracks a champion, how will I know if the cooldown time is wrong? You can simply go to the u.gg or op.gg website and search for the champion and see the cooldown time of their abilities. They have a huge team and a much more structured database for this, as they update manually with each update and don't depend entirely on this Data DDragon site.

----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

4 - So what is the point of the Tracker if it can give me incorrect information about the cooldown time of some champion? Okay, in the provided code that you can edit yourself, here's the code here: <img width="1332" height="882" alt="image" src="https://github.com/user-attachments/assets/90fa2f70-0438-4f26-b3d1-a87deb2b5e25" />

*How does this work? This highlighted command allows us to permanently change the outdated champion data to the new champion cooldown data found on the "u.gg" or "op.gg" websites.*

*As you can see in the highlighted command, there are already some comments explaining how it's done. Just add the command: "put("ChampionName", new String[]{"Q", "W", "E", "R"});"*

*Then replace the values ​​as shown in the command example and finally, rebuild the code to load this new change.* It's almost like what the teams at u.gg and op.gg do.*

------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

NOTE: If you want to disable the overlay of these spells, just go to the code and comment out these parts here:

<img width="1228" height="566" alt="image" src="https://github.com/user-attachments/assets/3a0bc156-f6d7-4973-9292-eb6efac29c9c" />





