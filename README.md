# ゆっくり数独

おばあちゃん向けの、Kotlin / Jetpack Compose製のAndroidアプリです。
Android 14以降に対応します。通常は盤面の下に数字を並べ、高さの小さい横長画面では横に並べます。
プレイ画面はスクロールせず使えるよう、残りの画面領域に合わせて盤面を調整します。

## 初期版の機能

- 既存Pythonジェネレーターで生成・評価した、6段階×2問の同梱JSON
- マスを選んでから数字を入力。最初からある数字は編集不可
- 選択マスと同じ行・列・3×3ブロックを薄く強調。重複だけを赤字・下線で表示
- 行・列・ブロックは青灰色、同じ数字は薄い黄色、選択マスは緑の枠で区別
- 最初からある数字は黒（ダークモードでは白）、入力した数字は青で表示
- 数字ボタン1〜9を一列に配置。メモモードでも長押しで数字を入力（モードは維持）
- 操作は盤面の上にアイコン＋短いラベルで配置。ホーム、Undo、Redo、メモ、消しゴム
- 9個以上置いた数字ボタンは色を控えめに表示するが、仮置きできるよう入力は継続可能
- 候補メモ、元に戻す、やり直す、同じ数字の強調、消去
- 重複なく全マスが埋まったときのクリア表示。ミスによるゲームオーバーなし
- 端末ごとに進行中1問。新しい問題への切り替え前に確認
- 入力・候補メモ・操作履歴（最大200回）・選択マス・メモモード・経過時間を自動保存
- アプリの再起動・プロセス再生成後も同じ問題の続きから再開
- 経過時間はプレイ画面が前面にある間だけ計測。時間制限なし
- システムのダークモード対応、読み上げ用のマス位置・値・重複・メモ情報

ヒント、クラウド取得、アカウント、実績共有、デイリー問題は今後の追加対象です。
同梱問題をすべて遊んだ難易度は既存問題を再出題します。

## 自動候補

難易度を選んだ後、開始ダイアログで自動候補のON/OFFを選びます。初期値はOFFです。
開始後の変更はできません。再起動してもゲーム開始時の設定を維持します。

- OFFでは従来の手書きメモを使います。他のマスへの入力で手書きメモを変更しません。
- ONでは、現在の行・列・ブロックと重複しない数字を空きマスに自動表示します。
- 正解や高度な手筋を参照せず、数字の入力・消去・Undo・Redoに合わせて再計算します。
- メモモードで数字を押すと、自動候補を個別に消す／戻すことができます。
- 手で消した候補は他のマスを操作しても維持します。現在の盤面で不可能な候補は戻せません。
- 空きマスで消しゴムを押すと、選択マスで手動で消した候補だけをまとめて戻します。
- 数字が入ったマスでは、消しゴムで数字を消して自動候補を再表示します。
- 候補の削除・復活もUndo/Redoと自動保存の対象です。

従来の保存形式（schema_version 1）は、手書きメモと履歴を残して自動候補OFFで読み込みます。
新しい形式（schema_version 2）では、開始時の設定と手動で消した候補も保存します。

## ディレクトリ構成

```text
app/src/main/
├── assets/puzzles.json             # バージョン付き同梱カタログ
└── java/com/example/sudoku/
    ├── MainActivity.kt             # Androidエントリーポイントのみ
    ├── domain/
    │   ├── model/                  # 問題・難易度・プレイ状態
    │   ├── game/                   # 入力・履歴・重複・完成判定（純Kotlin）
    │   └── repository/             # 問題取得・端末保存のインターフェース
    ├── data/
    │   ├── AppContainer.kt         # 依存関係の組み立て
    │   ├── json/                   # Python形式と保存形式の変換・検証
    │   └── repository/             # assets読込・AtomicFile保存
    └── ui/
        ├── SudokuApp.kt            # 画面切り替え・ライフサイクル・確認ダイアログ
        ├── SudokuViewModel.kt      # 状態管理と操作・保存の調整
        ├── home/                   # 続きから・難易度選択
        ├── game/                   # 画面サイズに応じたプレイ画面
        ├── components/             # 盤面・候補・数字ボタンなど
        └── theme/                  # 配色・文字
tools/export_puzzles.py             # Python生成器からJSONを書き出す補助ツール
tools/vendor_material_icons.py      # 使用するGoogle公式アイコンの取得
```

UI → ViewModel → domainの操作・repository契約 → data実装、という関係です。
画面はファイルやPythonを直接扱いません。
[Android公式の状態管理指針](https://developer.android.com/develop/ui/compose/state-hoisting)
を参考に、画面に渡す状態とイベントを分けています。

## ビルド・検証

Android Studioでこのフォルダを開き、Gradle Sync後にRunします。
コマンドラインではAndroid Studio同梱JDKなど、Gradle互換のJDKを指定します。

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
# エミュレーターまたは端末を接続して実行
.\gradlew.bat :app:connectedDebugAndroidTest
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

純Kotlinのテストは重複判定、正解と異なる入力の許容、固定マス保護、メモと履歴、
履歴上限、完成判定、自動候補の計算・手動削除・復活を確認します。端末テストはJSON検証、
旧形式の読み込み、保存復元、開始設定、実際の画面操作を確認します。

アイコンは[Google Material Symbols](https://github.com/google/material-design-icons)を同梱しています。
ライセンスは `app/src/main/assets/licenses/material-symbols.txt` に保存しています。
アプリでの表示にネット接続は不要です。

## 問題JSONの更新

Python生成器は別プロジェクトのまま使います。Android端末上では実行しません。

```powershell
python tools/export_puzzles.py --generator-root C:\Users\takuj\dev-root\aws-projects\sudoku-generator
```

既存の `Generator` と `Grader` を使って問題を生成し、**実際に評価されたレベル**ごとに
既定で2問ずつ採用します。`--per-level`、`--seed`、`--max-candidates`で調整できます。
すべての難易度が揃うまで出力ファイルは置き換えません。
JSONを更新後、APKを再ビルドして上書きインストールします。
同じapplicationIdと署名による上書き更新なら、進行中の問題は端末に残ります。
アンインストールやアプリデータ削除は途中経過も削除します。

```json
{
  "schema_version": 1,
  "puzzles": [
    {
      "id": "盤面のSHA256から作った固定ID",
      "puzzle": [["実データは9×9の整数配列。0が空きマス"]],
      "solution": [["実データは9×9の整数配列。1〜9"]],
      "rating": {
        "status": "rated",
        "level": 1,
        "evaluator_version": "1.0.0"
      }
    }
  ]
}
```

上記は構造説明用です。実際の盤面は同梱JSONを参照してください。
Python CLIの `--grade` 出力に `id` を追加し、`puzzles` 配列にまとめる形式です。
`holes`、スコア、手筋などの追加メタデータは保持できます。
`rated`以外、盤面の形・数字の範囲・正解の重複・初期数字と正解の不一致・IDの重複は受け付けません。
一意性と難易度の保証はPython側が担当します。

## 保存と今後のクラウド対応

`filesDir/progress.json` に、問題とプレイヤーの状態を別の項目として保存します。
進行中の問題自体も含めるため、次のAPKで同梱問題が変わっても再開できます。
`AtomicFile`で置き換え、IOスレッドの直列処理で古い保存が新しい保存を追い越すのを防ぎます。
入力時・画面を離れたとき・約5秒ごとの経過時間更新時に保存を要求します。
書き込み失敗は画面に表示し再試行できます。不正な保存データを無断で空データに置き換えません。

クラウド導入時は `PuzzleRepository` にAPI取得と端末キャッシュを実装し、
`AppContainer`で差し替えます。AWS/GCP固有の型は画面やゲームロジックへ持ち込みません。
問題IDと評価器バージョンは固定して保存します。プレイ状態には開始・完了時刻と
実プレイ時間があり、将来の実績モデルへ接続できます。現段階では累計実績や
過去の完了履歴は集計しません。デイリー割り当て・実績同期のAPI契約はその際に追加します。
